package de.mistatee.erglog.data.auth

/**
 * Thrown when the Concept2 `/oauth/access_token` endpoint rejects a token exchange, e.g. an
 * expired Authorization Code or a revoked Refresh Token.
 */
class AuthException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
