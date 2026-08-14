package de.mistatee.erglog.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import de.mistatee.erglog.data.local.LocalTransactionRunner
import de.mistatee.erglog.data.local.dao.ResultDao
import de.mistatee.erglog.data.local.dao.ResultSyncStateDao
import de.mistatee.erglog.data.local.entities.ResultEntity
import de.mistatee.erglog.data.local.entities.ResultSyncStateEntity
import de.mistatee.erglog.data.local.entities.toEntity
import de.mistatee.erglog.data.remote.results.RemoteResultsDataSource

private const val FIRST_PAGE = 1

/**
 * Syncs the cached [ResultEntity] rows with Concept2's page-numbered `/api/users/me/results`
 * endpoint. Room ([ResultDao]) is the source of truth the UI observes; this mediator is the only
 * thing that talks to the network. See docs/adr/0005-room-for-offline-first-persistence.md.
 *
 * The API has no per-item cursor, only `current_page`/`total_pages`, so pagination progress is
 * tracked in a single [ResultSyncStateEntity] row rather than per-item remote keys. On REFRESH the
 * cache and sync state are cleared and reloaded from page 1, matching the API's lack of a stable
 * "refresh key" concept.
 */
@OptIn(ExperimentalPagingApi::class)
class ResultRemoteMediator(
    private val resultDao: ResultDao,
    private val resultSyncStateDao: ResultSyncStateDao,
    private val remoteResultsDataSource: RemoteResultsDataSource,
    private val authRepository: AuthRepository,
    private val transactionRunner: LocalTransactionRunner,
) : RemoteMediator<Int, ResultEntity>() {
    /**
     * Always launches an initial REFRESH when a new [androidx.paging.Pager] is created. Combined
     * with [de.mistatee.erglog.ui.main.MainScreenViewModel] creating that Pager exactly once per
     * ViewModel lifetime (`cachedIn(viewModelScope)`), this is what gives refresh-on-launch its
     * "once per app launch" semantics; pull-to-refresh triggers a second REFRESH explicitly.
     */
    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ResultEntity>): MediatorResult {
        val pageToLoad = pageToLoad(loadType) ?: return MediatorResult.Success(endOfPaginationReached = true)

        return authRepository.validSession().fold(
            onSuccess = { session ->
                fetchAndCachePage(loadType, pageToLoad, session.accessToken, state.config.pageSize)
            },
            onFailure = { MediatorResult.Error(it) },
        )
    }

    /** The page to fetch for [loadType], or `null` if pagination should stop without a network call. */
    private suspend fun pageToLoad(loadType: LoadType): Int? =
        when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE
            LoadType.APPEND -> resultSyncStateDao.get()?.nextPage
            LoadType.PREPEND -> null
        }

    @Suppress("TooGenericExceptionCaught") // Boundary between the network call and Paging's MediatorResult.Error.
    private suspend fun fetchAndCachePage(
        loadType: LoadType,
        page: Int,
        accessToken: String,
        pageSize: Int,
    ): MediatorResult =
        try {
            val resultPage =
                remoteResultsDataSource.fetchResults(accessToken = accessToken, page = page, pageSize = pageSize)
            val endOfPaginationReached = resultPage.currentPage >= resultPage.totalPages

            transactionRunner.runInTransaction {
                if (loadType == LoadType.REFRESH) {
                    resultDao.clear()
                    resultSyncStateDao.clear()
                }
                resultDao.upsertAll(resultPage.results.map { it.toEntity() })
                resultSyncStateDao.upsert(
                    ResultSyncStateEntity(
                        lastLoadedPage = resultPage.currentPage,
                        totalPages = resultPage.totalPages,
                        endOfPaginationReached = endOfPaginationReached,
                    ),
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (throwable: Exception) {
            MediatorResult.Error(throwable)
        }
}
