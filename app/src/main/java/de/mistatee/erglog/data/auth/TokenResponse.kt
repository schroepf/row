package de.mistatee.erglog.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Successful response body from the Concept2 `/oauth/access_token` endpoint, used both for the
 * initial Authorization Code exchange and for Refresh Token exchanges.
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
)

/** Error response body from the Concept2 `/oauth/access_token` endpoint (HTTP 400/401). */
@Serializable
data class TokenErrorResponse(
    val error: String,
    @SerialName("error_description") val errorDescription: String? = null,
)
