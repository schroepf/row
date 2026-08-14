package de.mistatee.erglog.data.remote.results.model

import kotlinx.serialization.Serializable

/**
 * Response body from the Concept2 `/api/users/me/results` endpoint. Only the fields currently
 * needed by the app are modeled; the full response has additional fields not yet used here.
 */
@Serializable
data class RemoteResultsResponse(
    val data: List<RemoteResultResponse>,
    val meta: RemoteMetaResponse,
)
