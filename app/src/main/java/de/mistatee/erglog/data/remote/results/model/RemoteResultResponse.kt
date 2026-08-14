package de.mistatee.erglog.data.remote.results.model

import kotlinx.serialization.Serializable

@Serializable
data class RemoteResultResponse(
    val id: Long,
    val date: String,
    val distance: Int,
    val type: String,
    val time: Long,
)
