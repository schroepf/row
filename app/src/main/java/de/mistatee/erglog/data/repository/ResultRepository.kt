package de.mistatee.erglog.data.repository

import androidx.paging.PagingData
import de.mistatee.erglog.data.model.Result
import kotlinx.coroutines.flow.Flow

interface ResultRepository {
    /**
     * Observes the Logbook Account holder's cached Result History, backed by Room. Loading more
     * pages and syncing with Concept2 happens transparently as the returned [PagingData] is
     * scrolled; see [ResultRemoteMediator].
     */
    fun observeResults(): Flow<PagingData<Result>>
}
