package com.schroepf.row

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface RowApi {
    suspend fun fetchUserProfile(authorizationCode: String): RowProfile
}

class KtorRowApi(
    private val client: HttpClient,
    private val authConfig: Concept2AuthConfig
) : RowApi {
    override suspend fun fetchUserProfile(authorizationCode: String): RowProfile {
        val tokenResponse = client.submitForm(
            url = CONCEPT2_TOKEN_ENDPOINT,
            formParameters = Parameters.build {
                append("client_id", authConfig.clientId)
                append("client_secret", authConfig.clientSecret)
                append("code", authorizationCode)
                append("grant_type", "authorization_code")
                append("redirect_uri", authConfig.redirectUri.toString())
                append("scope", concept2Scope())
            }
        ).requireSuccess<Concept2TokenResponse>("Concept2 token exchange")

        val profileResponse = client.get(CONCEPT2_PROFILE_ENDPOINT) {
            bearerAuth(tokenResponse.accessToken)
            accept(ContentType.parse("application/vnd.c2logbook.v1+json"))
        }.requireSuccess<Concept2UserEnvelope>("Concept2 profile request")

        return profileResponse.data.toProfile()
    }
}

@Serializable
data class Concept2TokenResponse(
    @SerialName("access_token")
    val accessToken: String
)

@Serializable
data class Concept2UserEnvelope(
    @SerialName("data")
    val data: Concept2UserResponse
)

@Serializable
data class Concept2UserResponse(
    @SerialName("username")
    val username: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("country")
    val country: String? = null
)

@Serializable
data class Concept2ErrorResponse(
    @SerialName("error_description")
    val errorDescription: String? = null
)

data class RowProfile(
    val username: String,
    val fullName: String,
    val email: String?,
    val country: String?
)

private suspend inline fun <reified T> io.ktor.client.statement.HttpResponse.requireSuccess(
    operationName: String
): T {
    if (!status.isSuccess()) {
        val error = runCatching { body<Concept2ErrorResponse>() }.getOrNull()
        throw IllegalStateException(error?.errorDescription ?: "$operationName failed with HTTP $status")
    }
    return body()
}

private fun Concept2UserResponse.toProfile(): RowProfile {
    val fullName = listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
        .joinToString(" ")
        .ifBlank { username }
    return RowProfile(
        username = username,
        fullName = fullName,
        email = email,
        country = country
    )
}
