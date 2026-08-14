package de.mistatee.erglog.data.remote.auth

import de.mistatee.erglog.BuildConfig
import de.mistatee.erglog.data.remote.auth.model.RemoteAuthException
import de.mistatee.erglog.data.remote.auth.model.RemoteTokenErrorResponse
import de.mistatee.erglog.data.remote.auth.model.RemoteTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters

private const val AUTHORIZE_URL = "https://log.concept2.com/oauth/authorize"
private const val TOKEN_URL = "https://log.concept2.com/oauth/access_token"
private const val SCOPE = "user:read,results:read"
private const val REDIRECT_URI = "de.mistatee.erglog://authorization/callback"

class Concept2RemoteAuthDataSource(
    private val httpClient: HttpClient,
) : RemoteAuthDataSource {
    override val authorizationUrl: String
        get() = AUTHORIZE_URL +
                "?client_id=${BuildConfig.C2_CLIENT_ID}" +
                "&scope=$SCOPE" +
                "&response_type=code" +
                "&redirect_uri=$REDIRECT_URI"

    override suspend fun exchangeAuthorizationCode(code: String): RemoteTokenResponse =
        requestToken(
            Parameters.build {
                append("client_id", BuildConfig.C2_CLIENT_ID)
                append("client_secret", BuildConfig.C2_CLIENT_SECRET)
                append("grant_type", "authorization_code")
                append("scope", SCOPE)
                append("code", code)
                append("redirect_uri", REDIRECT_URI)
            },
        )

    override suspend fun refreshAccessToken(refreshToken: String): RemoteTokenResponse =
        requestToken(
            Parameters.build {
                append("client_id", BuildConfig.C2_CLIENT_ID)
                append("client_secret", BuildConfig.C2_CLIENT_SECRET)
                append("grant_type", "refresh_token")
                append("scope", SCOPE)
                append("refresh_token", refreshToken)
            },
        )

    private suspend fun requestToken(formParameters: Parameters): RemoteTokenResponse {
        val response = httpClient.submitForm(url = TOKEN_URL, formParameters = formParameters)
        if (response.status != HttpStatusCode.OK) {
            throw response.toAuthException()
        }
        return response.body()
    }

    private suspend fun HttpResponse.toAuthException(): RemoteAuthException {
        val errorBody = runCatching { body<RemoteTokenErrorResponse>() }
        val parsed = errorBody.getOrNull()
        return RemoteAuthException(
            error = parsed?.error ?: "http_${status.value}",
            errorDescription = parsed?.errorDescription,
        )
    }
}
