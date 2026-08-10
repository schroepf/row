package de.mistatee.erglog.data.auth

import de.mistatee.erglog.BuildConfig
import de.mistatee.erglog.data.defaultHttpClient
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

/** Builds the URL that opens Concept2's hosted login/consent page in a Custom Tab. */
fun buildAuthorizationUrl(): String =
    "$AUTHORIZE_URL" +
        "?client_id=${BuildConfig.C2_CLIENT_ID}" +
        "&scope=$SCOPE" +
        "&response_type=code" +
        "&redirect_uri=$REDIRECT_URI"

/**
 * Talks to Concept2's OAuth2 token endpoint to exchange an Authorization Code or Refresh Token
 * for an Access Token. See https://log.concept2.com/developers/documentation/#authentication.
 */
interface AuthApi {
    suspend fun exchangeAuthorizationCode(code: String): TokenResponse

    suspend fun refreshAccessToken(refreshToken: String): TokenResponse
}

class KtorAuthApi(
    private val httpClient: HttpClient = defaultHttpClient(),
) : AuthApi {
    override suspend fun exchangeAuthorizationCode(code: String): TokenResponse =
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

    override suspend fun refreshAccessToken(refreshToken: String): TokenResponse =
        requestToken(
            Parameters.build {
                append("client_id", BuildConfig.C2_CLIENT_ID)
                append("client_secret", BuildConfig.C2_CLIENT_SECRET)
                append("grant_type", "refresh_token")
                append("scope", SCOPE)
                append("refresh_token", refreshToken)
            },
        )

    private suspend fun requestToken(formParameters: Parameters): TokenResponse {
        val response = httpClient.submitForm(url = TOKEN_URL, formParameters = formParameters)
        if (response.status != HttpStatusCode.OK) {
            throw response.toAuthException()
        }
        return response.body()
    }

    private suspend fun HttpResponse.toAuthException(): AuthException {
        val errorBody = runCatching { body<TokenErrorResponse>() }
        val parsed = errorBody.getOrNull()
        return AuthException(
            error = parsed?.error ?: "http_${status.value}",
            errorDescription = parsed?.errorDescription,
        )
    }
}
