package de.mistatee.erglog.data.concept2.logbook.results

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.mistatee.erglog.data.concept2.logbook.results.model.Result

private const val FIRST_PAGE = 1

/** Loads pages of Result History from [resultRepository] for use with a Compose [androidx.paging.Pager]. */
class ResultPagingSource(
    private val resultRepository: ResultRepository,
) : PagingSource<Int, Result>() {
    // Always restart from the first page on invalidation; the API has no stable "refresh key" concept.
    override fun getRefreshKey(state: PagingState<Int, Result>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Result> {
        val page = params.key ?: FIRST_PAGE
        return resultRepository.fetchPage(page, params.loadSize).fold(
            onSuccess = { resultPage ->
                LoadResult.Page(
                    data = resultPage.results,
                    prevKey = if (resultPage.currentPage > FIRST_PAGE) resultPage.currentPage - 1 else null,
                    nextKey = if (resultPage.hasNextPage) resultPage.currentPage + 1 else null,
                )
            },
            onFailure = { throwable -> LoadResult.Error(throwable) },
        )
    }
}
