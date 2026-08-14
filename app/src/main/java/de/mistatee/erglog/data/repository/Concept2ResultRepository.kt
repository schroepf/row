package de.mistatee.erglog.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import de.mistatee.erglog.data.local.dao.ResultDao
import de.mistatee.erglog.data.local.entities.toDomain
import de.mistatee.erglog.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PAGE_SIZE = 20

@OptIn(ExperimentalPagingApi::class)
class Concept2ResultRepository(
    private val resultDao: ResultDao,
    private val remoteMediator: ResultRemoteMediator,
) : ResultRepository {
    override fun observeResults(): Flow<PagingData<Result>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = remoteMediator,
            pagingSourceFactory = { resultDao.pagingSource() },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
}
