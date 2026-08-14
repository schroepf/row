package de.mistatee.erglog.data.model

import kotlin.time.Duration
import kotlin.time.Instant

/** A single logged result, as returned by Concept2's `/api/users/me/results` endpoint. */
data class Result(
    val id: Long,
    val date: Instant,
    val distance: Int,
    val type: String,
    val duration: Duration,
)
