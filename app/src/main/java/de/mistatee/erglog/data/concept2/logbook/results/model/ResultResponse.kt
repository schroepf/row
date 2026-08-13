package de.mistatee.erglog.data.concept2.logbook.results.model

import kotlinx.serialization.Serializable

@Serializable
data class ResultResponse(
    val id: Long,
    val date: String,
    val distance: Int,
    val type: String,
    val time: Long,
)
