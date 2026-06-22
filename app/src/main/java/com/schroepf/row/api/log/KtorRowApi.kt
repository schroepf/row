package com.schroepf.row.api.log

import com.schroepf.row.api.ApiConstants
import com.schroepf.row.api.auth.Concept2AuthConfig
import com.schroepf.row.api.auth.Concept2TokenResponse
import com.schroepf.row.api.auth.requireSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Parameters

class KtorRowApi(
    private val client: HttpClient,
    private val authConfig: Concept2AuthConfig
) : RowApi {
    override suspend fun fetchUserProfile(authorizationCode: String, codeVerifier: String?): UserProfile {
        val tokenResponse = client.submitForm(
            url = ApiConstants.CONCEPT2_TOKEN_ENDPOINT,
            formParameters = Parameters.build {
                append("client_id", authConfig.clientId)
                append("client_secret", authConfig.clientSecret)
                append("code", authorizationCode)
                append("grant_type", "authorization_code")
                append("redirect_uri", authConfig.redirectUri.toString())
                append("scope", ApiConstants.CONCEPT2_SCOPE)
                if (codeVerifier != null) append("code_verifier", codeVerifier)
            }
        ).requireSuccess<Concept2TokenResponse>("Concept2 token exchange")

        val profileResponse = client.get(ApiConstants.CONCEPT2_PROFILE_ENDPOINT) {
            bearerAuth(tokenResponse.accessToken)
            accept(ContentType.parse(ApiConstants.CONCEPT2_PROFILE_ACCEPT))
        }.requireSuccess<Concept2UserEnvelope>("Concept2 profile request")

        return profileResponse.data.toUserProfile()
    }
}
