package de.mistatee.erglog.data.remote.results.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePaginationResponse(
    val total: Int,
    val count: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("current_page") val currentPage: Int,
    @SerialName("total_pages") val totalPages: Int,
)
