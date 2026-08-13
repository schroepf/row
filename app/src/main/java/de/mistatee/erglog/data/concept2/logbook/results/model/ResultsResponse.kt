package de.mistatee.erglog.data.concept2.logbook.results.model

import kotlinx.serialization.Serializable

/**
 * Response body from the Concept2 `/api/users/me/results` endpoint. Only the fields currently
 * needed by the app are modeled; the full response has additional fields not yet used here.
 */
@Serializable
data class ResultsResponse(
    val data: List<ResultResponse>,
    val meta: MetaResponse,
)
