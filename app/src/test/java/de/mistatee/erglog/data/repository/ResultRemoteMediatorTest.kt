package de.mistatee.erglog.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.local.LocalTransactionRunner
import de.mistatee.erglog.data.local.dao.ResultDao
import de.mistatee.erglog.data.local.dao.ResultSyncStateDao
import de.mistatee.erglog.data.local.entities.ResultEntity
import de.mistatee.erglog.data.local.entities.ResultSyncStateEntity
import de.mistatee.erglog.data.local.entities.toEntity
import de.mistatee.erglog.data.model.ResultPage
import de.mistatee.erglog.data.model.SessionError
import de.mistatee.erglog.data.remote.results.RemoteResultsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class ResultRemoteMediatorTest {
    private val config = PagingConfig(pageSize = 20, enablePlaceholders = false)
    private val transactionRunner = object : LocalTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }
    private val session = MockData.Auth.session

    private fun pagingState() = PagingState<Int, ResultEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = config,
        leadingPlaceholderCount = 0,
    )

    @Test
    fun `REFRESH clears cache, upserts first page, and saves sync state on success`() = runTest {
        // given
        val firstPage =
            ResultPage(results = MockData.Api.Results.results, currentPage = 1, totalPages = 2)
        val resultDao = mockk<ResultDao>(relaxUnitFun = true)
        val syncStateDao = mockk<ResultSyncStateDao>(relaxUnitFun = true)
        val mediator = ResultRemoteMediator(
            resultDao = resultDao,
            resultSyncStateDao = syncStateDao,
            remoteResultsDataSource = mockk<RemoteResultsDataSource> {
                coEvery {
                    fetchResults(
                        session.accessToken,
                        page = 1,
                        pageSize = config.pageSize,
                    )
                } returns firstPage
            },
            authRepository = mockk<AuthRepository> {
                coEvery { validSession() } returns Result.success(session)
            },
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.REFRESH, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
        coVerify(exactly = 1) { resultDao.clear() }
        coVerify(exactly = 1) { syncStateDao.clear() }
        coVerify(exactly = 1) { resultDao.upsertAll(MockData.Api.Results.results.map { it.toEntity() }) }
        coVerify(exactly = 1) {
            syncStateDao.upsert(
                ResultSyncStateEntity(
                    lastLoadedPage = 1,
                    totalPages = 2,
                    endOfPaginationReached = false,
                ),
            )
        }
    }

    @Test
    fun `REFRESH reports endOfPaginationReached when only one page exists`() = runTest {
        // given
        val onlyPage =
            ResultPage(results = MockData.Api.Results.results, currentPage = 1, totalPages = 1)
        val mediator = ResultRemoteMediator(
            resultDao = mockk<ResultDao>(relaxUnitFun = true),
            resultSyncStateDao = mockk<ResultSyncStateDao>(relaxUnitFun = true),
            remoteResultsDataSource = mockk<RemoteResultsDataSource> {
                coEvery {
                    fetchResults(
                        session.accessToken,
                        page = 1,
                        pageSize = config.pageSize,
                    )
                } returns onlyPage
            },
            authRepository = mockk<AuthRepository> {
                coEvery { validSession() } returns Result.success(session)
            },
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.REFRESH, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun `APPEND fetches next page based on saved sync state`() = runTest {
        // given
        val secondPage =
            ResultPage(results = MockData.Api.Results.results, currentPage = 2, totalPages = 2)
        val resultDao = mockk<ResultDao>(relaxUnitFun = true)
        val syncStateDao = mockk<ResultSyncStateDao>(relaxUnitFun = true) {
            coEvery { get() } returns
                    ResultSyncStateEntity(
                        lastLoadedPage = 1,
                        totalPages = 2,
                        endOfPaginationReached = false,
                    )
        }
        val mediator = ResultRemoteMediator(
            resultDao = resultDao,
            resultSyncStateDao = syncStateDao,
            remoteResultsDataSource = mockk<RemoteResultsDataSource> {
                coEvery {
                    fetchResults(
                        session.accessToken,
                        page = 2,
                        pageSize = config.pageSize,
                    )
                } returns secondPage
            },
            authRepository = mockk<AuthRepository> {
                coEvery { validSession() } returns Result.success(session)
            },
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.APPEND, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
        coVerify(exactly = 0) { resultDao.clear() }
        coVerify(exactly = 1) { resultDao.upsertAll(MockData.Api.Results.results.map { it.toEntity() }) }
        coVerify(exactly = 1) {
            syncStateDao.upsert(
                ResultSyncStateEntity(
                    lastLoadedPage = 2,
                    totalPages = 2,
                    endOfPaginationReached = true,
                ),
            )
        }
    }

    @Test
    fun `APPEND ends pagination immediately when saved sync state already reached the end`() =
        runTest {
            // given
            val remoteResultsDataSource = mockk<RemoteResultsDataSource>()
            val mediator = ResultRemoteMediator(
                resultDao = mockk(),
                resultSyncStateDao = mockk {
                    coEvery { get() } returns
                            ResultSyncStateEntity(
                                lastLoadedPage = 2,
                                totalPages = 2,
                                endOfPaginationReached = true,
                            )
                },
                remoteResultsDataSource = remoteResultsDataSource,
                authRepository = mockk(),
                transactionRunner = transactionRunner,
            )

            // when
            val result = mediator.load(LoadType.APPEND, pagingState())

            // then
            assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
            assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
            coVerify(exactly = 0) { remoteResultsDataSource.fetchResults(any(), any(), any()) }
        }

    @Test
    fun `PREPEND immediately ends pagination without calling the API`() = runTest {
        // given
        val remoteResultsDataSource = mockk<RemoteResultsDataSource>()
        val mediator = ResultRemoteMediator(
            resultDao = mockk(),
            resultSyncStateDao = mockk(),
            remoteResultsDataSource = remoteResultsDataSource,
            authRepository = mockk(),
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.PREPEND, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Success>()
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
        coVerify(exactly = 0) { remoteResultsDataSource.fetchResults(any(), any(), any()) }
    }

    @Test
    fun `REFRESH returns Error when session is invalid`() = runTest {
        // given
        val mediator = ResultRemoteMediator(
            resultDao = mockk(),
            resultSyncStateDao = mockk(),
            remoteResultsDataSource = mockk(),
            authRepository = mockk {
                coEvery { validSession() } returns Result.failure(SessionError.NoSession)
            },
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.REFRESH, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Error>()
        assertThat((result as RemoteMediator.MediatorResult.Error).throwable).isEqualTo(SessionError.NoSession)
    }

    @Test
    fun `REFRESH returns Error when the API throws`() = runTest {
        // given
        val apiFailure = IllegalStateException("boom")
        val mediator = ResultRemoteMediator(
            resultDao = mockk(),
            resultSyncStateDao = mockk(),
            remoteResultsDataSource = mockk {
                coEvery {
                    fetchResults(
                        session.accessToken,
                        page = 1,
                        pageSize = config.pageSize,
                    )
                } throws apiFailure
            },
            authRepository = mockk {
                coEvery { validSession() } returns Result.success(session)
            },
            transactionRunner = transactionRunner,
        )

        // when
        val result = mediator.load(LoadType.REFRESH, pagingState())

        // then
        assertThat(result).isInstanceOf<RemoteMediator.MediatorResult.Error>()
        assertThat((result as RemoteMediator.MediatorResult.Error).throwable).isEqualTo(apiFailure)
    }
}
