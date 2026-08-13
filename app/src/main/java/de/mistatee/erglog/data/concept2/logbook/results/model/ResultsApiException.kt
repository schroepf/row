package de.mistatee.erglog.data.concept2.logbook.results.model

/** Thrown when the Concept2 `/api/users/me/results` endpoint rejects or fails a result fetch. */
class ResultsApiException(
    val error: String,
    val errorDescription: String?,
) : Exception("$error: $errorDescription")
