package de.mistatee.erglog.data.concept2.logbook.results

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.paging.map
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import de.mistatee.erglog.data.local.results.ResultDao
import de.mistatee.erglog.data.local.results.ResultEntity
import de.mistatee.erglog.data.local.results.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PAGE_SIZE = 20

@OptIn(ExperimentalPagingApi::class)
class Concept2ResultRepository(
    private val resultDao: ResultDao,
    private val remoteMediator: RemoteMediator<Int, ResultEntity>,
) : ResultRepository {
    override fun observeResults(): Flow<PagingData<Result>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = remoteMediator,
            pagingSourceFactory = { resultDao.pagingSource() },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
}
