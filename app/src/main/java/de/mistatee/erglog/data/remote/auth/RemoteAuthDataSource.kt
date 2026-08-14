package de.mistatee.erglog.data.remote.auth

import de.mistatee.erglog.data.remote.auth.model.RemoteTokenResponse

/**
 * Talks to Concept2's OAuth2 token endpoint to exchange an Authorization Code or Refresh Token
 * for an Access Token. See https://log.concept2.com/developers/documentation/#authentication.
 */
interface RemoteAuthDataSource {
    val authorizationUrl: String

    suspend fun exchangeAuthorizationCode(code: String): RemoteTokenResponse

    suspend fun refreshAccessToken(refreshToken: String): RemoteTokenResponse
}
