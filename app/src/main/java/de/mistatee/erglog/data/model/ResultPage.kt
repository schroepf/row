package de.mistatee.erglog.data.model

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
