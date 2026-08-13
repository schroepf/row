package de.mistatee.erglog.data.concept2.logbook.results

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResultPagingSourceTest {
    private val config = PagingConfig(pageSize = 20, enablePlaceholders = false)

    @Test
    fun `returns first page with results on success`() =
        runTest {
            // Given
            val results = MockData.Api.Results.results
            val page = ResultPage(results = results, currentPage = 1, totalPages = 2)

            val pager = TestPager(
                config = config,
                pagingSource = ResultPagingSource(
                    resultRepository = mockk {
                        coEvery { fetchPage(page = 1, pageSize = any()) } returns Result.success(page)
                    },
                ),
            )

            // When
            val result = pager.refresh() as PagingSource.LoadResult.Page

            // Then
            assertThat(result.data).isEqualTo(results)
            assertThat(result.nextKey).isEqualTo(2)
        }

    @Test
    fun `appends next page results on success`() =
        runTest {
            // Given
            val firstPageResults = listOf(MockData.Api.Results.results.first())
            val secondPageResults = MockData.Api.Results.results
            val firstPage = ResultPage(results = firstPageResults, currentPage = 1, totalPages = 2)
            val secondPage = ResultPage(results = secondPageResults, currentPage = 2, totalPages = 2)

            val pager = TestPager(
                config = config,
                pagingSource = ResultPagingSource(
                    resultRepository = mockk {
                        coEvery { fetchPage(page = 1, pageSize = any()) } returns Result.success(firstPage)
                        coEvery { fetchPage(page = 2, pageSize = any()) } returns Result.success(secondPage)
                    },
                ),
            )

            pager.refresh()

            // When
            val result = pager.append() as PagingSource.LoadResult.Page

            // Then
            assertThat(result.data).isEqualTo(secondPageResults)
            assertThat(result.nextKey).isNull()
        }

    @Test
    fun `returns failure with underlying exception on failure`() =
        runTest {
            // Given
            val exception = IllegalStateException("boom")
            val pager = TestPager(
                config = config,
                pagingSource = ResultPagingSource(
                    resultRepository = mockk {
                        coEvery { fetchPage(page = 1, pageSize = any()) } returns Result.failure(exception)
                    },
                ),
            )

            // When
            val result = pager.refresh() as PagingSource.LoadResult.Error

            // Then
            assertThat(result.throwable).isEqualTo(exception)
        }

    @Test
    fun `returns null next key when there is no next page`() =
        runTest {
            // Given
            val onlyPage = ResultPage(results = MockData.Api.Results.results, currentPage = 1, totalPages = 1)

            val pager = TestPager(
                config = config,
                pagingSource = ResultPagingSource(
                    resultRepository = mockk {
                        coEvery { fetchPage(page = 1, pageSize = any()) } returns Result.success(onlyPage)
                    },
                ),
            )

            // When
            val result = pager.refresh() as PagingSource.LoadResult.Page

            // Then
            assertThat(result.nextKey).isEqualTo(null)
        }
}
