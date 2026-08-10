package de.mistatee.erglog.data.profile

/** Thrown when the Concept2 `/api/users/{user}` endpoint rejects or fails a profile fetch. */
class ProfileException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
