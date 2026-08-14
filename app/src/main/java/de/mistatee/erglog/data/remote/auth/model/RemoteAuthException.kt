package de.mistatee.erglog.data.remote.auth.model

/**
 * Thrown when the Concept2 `/oauth/access_token` endpoint rejects a token exchange, e.g. an
 * expired Authorization Code or a revoked Refresh Token.
 */
class RemoteAuthException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
