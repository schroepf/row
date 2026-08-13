package de.mistatee.erglog.data.concept2.auth.api

import de.mistatee.erglog.data.concept2.auth.model.TokenResponse

/**
 * Talks to Concept2's OAuth2 token endpoint to exchange an Authorization Code or Refresh Token
 * for an Access Token. See https://log.concept2.com/developers/documentation/#authentication.
 */
interface AuthApi {
    val authorizationUrl: String

    suspend fun exchangeAuthorizationCode(code: String): TokenResponse

    suspend fun refreshAccessToken(refreshToken: String): TokenResponse
}
