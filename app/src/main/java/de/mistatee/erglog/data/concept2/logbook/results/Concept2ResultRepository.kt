package de.mistatee.erglog.data.concept2.logbook.results

import de.mistatee.erglog.data.concept2.auth.AuthRepository
import de.mistatee.erglog.data.concept2.logbook.results.api.ResultsApi
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage

class Concept2ResultRepository(
    private val authRepository: AuthRepository,
    private val resultsApi: ResultsApi,
) : ResultRepository {
    override suspend fun fetchPage(page: Int, pageSize: Int): Result<ResultPage> =
        authRepository.validSession().mapCatching { session ->
            resultsApi.fetchResults(session.accessToken, page, pageSize)
        }
}
