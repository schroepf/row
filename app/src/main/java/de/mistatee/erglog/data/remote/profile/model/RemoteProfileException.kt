package de.mistatee.erglog.data.remote.profile.model

/** Thrown when the Concept2 `/api/users/{user}` endpoint rejects or fails a profile fetch. */
class RemoteProfileException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
