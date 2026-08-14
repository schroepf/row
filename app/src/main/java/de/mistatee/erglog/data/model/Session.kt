package de.mistatee.erglog.data.model

import kotlin.time.Instant

/**
 * The app's local record that a Logbook Account holder is currently authenticated, backed by a
 * stored Access Token and Refresh Token.
 */
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiry: Instant,
)
