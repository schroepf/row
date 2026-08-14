package de.mistatee.erglog.data.results

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.testing.asSnapshot
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.logbook.results.Concept2ResultRepository
import de.mistatee.erglog.data.local.results.ResultDao
import de.mistatee.erglog.data.local.results.ResultEntity
import de.mistatee.erglog.data.local.results.toEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class Concept2ResultRepositoryTest {
    private class FakePagingSource(private val entities: List<ResultEntity>) : PagingSource<Int, ResultEntity>() {
        override fun getRefreshKey(state: PagingState<Int, ResultEntity>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ResultEntity> =
            LoadResult.Page(data = entities, prevKey = null, nextKey = null)
    }

    /** A no-op mediator: the cache is already "synced", so paging never needs to hit the network. */
    @OptIn(ExperimentalPagingApi::class)
    private class NoOpRemoteMediator : RemoteMediator<Int, ResultEntity>() {
        override suspend fun load(loadType: LoadType, state: PagingState<Int, ResultEntity>): MediatorResult =
            MediatorResult.Success(endOfPaginationReached = true)
    }

    @Test
    fun `observeResults emits Room-cached results mapped to the domain model`() = runTest {
        // given
        val cachedEntities = MockData.Api.Results.results.map { it.toEntity() }
        val resultDao = mockk<ResultDao> {
            every { pagingSource() } returns FakePagingSource(cachedEntities)
        }
        val repository = Concept2ResultRepository(
            resultDao = resultDao,
            remoteMediator = NoOpRemoteMediator(),
        )

        // when
        val results = repository.observeResults().asSnapshot()

        // then
        assertThat(results).isEqualTo(MockData.Api.Results.results)
    }
}
