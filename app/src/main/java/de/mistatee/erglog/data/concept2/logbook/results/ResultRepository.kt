package de.mistatee.erglog.data.concept2.logbook.results

import androidx.paging.PagingData
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import kotlinx.coroutines.flow.Flow

interface ResultRepository {
    /**
     * Observes the Logbook Account holder's cached Result History, backed by Room. Loading more
     * pages and syncing with Concept2 happens transparently as the returned [PagingData] is
     * scrolled; see [de.mistatee.erglog.data.local.results.ResultRemoteMediator].
     */
    fun observeResults(): Flow<PagingData<Result>>
}
