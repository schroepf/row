package de.mistatee.erglog.data.remote.results.model

/** Thrown when the Concept2 `/api/users/me/results` endpoint rejects or fails a result fetch. */
class RemoteResultsException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
