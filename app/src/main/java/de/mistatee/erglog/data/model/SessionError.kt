package de.mistatee.erglog.data.model

sealed class SessionError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    object NoSession : SessionError("No session")

    class RefreshFailed(cause: Throwable) : SessionError("Refresh failed: ${cause.message}", cause)
}
