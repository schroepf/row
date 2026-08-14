package de.mistatee.erglog.data.remote.results

import de.mistatee.erglog.data.model.ResultPage

/**
 * Talks to Concept2's `/api/users/me/results` endpoint to fetch a page of the Logbook Account
 * holder's Result History. See
 * https://log.concept2.com/developers/documentation/#logbook-users-results.
 */
interface RemoteResultsDataSource {
    suspend fun fetchResults(
        accessToken: String,
        page: Int,
        pageSize: Int,
    ): ResultPage
}

