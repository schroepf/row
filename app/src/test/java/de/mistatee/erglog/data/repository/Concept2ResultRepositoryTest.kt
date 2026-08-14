package de.mistatee.erglog.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.testing.asSnapshot
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.local.dao.ResultDao
import de.mistatee.erglog.data.local.entities.ResultEntity
import de.mistatee.erglog.data.local.entities.toEntity
import io.mockk.coEvery
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

    @Test
    fun `observeResults emits Room-cached results mapped to the domain model`() = runTest {
        // given
        val cachedEntities = MockData.Api.Results.results.map { it.toEntity() }
        val resultDao = mockk<ResultDao> {
            every { pagingSource() } returns FakePagingSource(cachedEntities)
        }
        val repository = Concept2ResultRepository(
            resultDao = resultDao,
            remoteMediator = mockk(relaxed = true) {
                coEvery {
                    load(
                        any(),
                        any(),
                    )
                } returns RemoteMediator.MediatorResult.Success(endOfPaginationReached = true)
            },
        )

        // when
        val results = repository.observeResults().asSnapshot()

        // then
        assertThat(results).isEqualTo(MockData.Api.Results.results)
    }
}
