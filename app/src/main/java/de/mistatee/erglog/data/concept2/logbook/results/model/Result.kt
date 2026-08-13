package de.mistatee.erglog.data.concept2.logbook.results.model

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

/**
 * A single page of the Logbook Account holder's full Result History, as returned by Concept2's
 * `/api/users/me/results` endpoint.
 */
data class ResultPage(
    val results: List<Result>,
    val currentPage: Int,
    val totalPages: Int,
) {
    val hasNextPage: Boolean get() = currentPage < totalPages
}
