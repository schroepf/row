package de.mistatee.erglog.data.concept2.logbook.results

import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage

interface ResultRepository {
    suspend fun fetchPage(page: Int, pageSize: Int): Result<ResultPage>
}
