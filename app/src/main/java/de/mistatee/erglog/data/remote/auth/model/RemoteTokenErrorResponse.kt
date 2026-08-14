package de.mistatee.erglog.data.remote.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Error response body from the Concept2 `/oauth/access_token` endpoint (HTTP 400/401). */
@Serializable
data class RemoteTokenErrorResponse(
    val error: String,
    @SerialName("error_description") val errorDescription: String? = null,
)
