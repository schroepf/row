package de.mistatee.erglog.data.remote.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Successful response body from the Concept2 `/oauth/access_token` endpoint, used both for the
 * initial Authorization Code exchange and for Refresh Token exchanges.
 */
@Serializable
data class RemoteTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
)

