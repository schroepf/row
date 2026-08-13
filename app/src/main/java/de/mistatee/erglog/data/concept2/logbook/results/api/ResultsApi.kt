package de.mistatee.erglog.data.concept2.logbook.results.api

import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage

/**
 * Talks to Concept2's `/api/users/me/results` endpoint to fetch a page of the Logbook Account
 * holder's Result History. See
 * https://log.concept2.com/developers/documentation/#logbook-users-results.
 */
interface ResultsApi {
    suspend fun fetchResults(
        accessToken: String,
        page: Int,
        pageSize: Int,
    ): ResultPage
}

