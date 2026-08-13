package de.mistatee.erglog.utils

import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf

private fun loadStates(
    refreshError: Throwable? = null,
    appendError: Throwable? = null,
    isAppendLoading: Boolean = false,
) = LoadStates(
    refresh = if (refreshError == null) LoadState.NotLoading(false) else LoadState.Error(refreshError),
    prepend = LoadState.NotLoading(false),
    append = when {
        appendError != null -> LoadState.Error(appendError)
        isAppendLoading -> LoadState.Loading
        else -> LoadState.NotLoading(false)
    },
)

@Composable
fun <T : Any> List<T>.asLazyPagingItems(
    isLoading: Boolean = false,
) = flowOf(
    PagingData.from(
        data = this,
        sourceLoadStates = loadStates(isAppendLoading = isLoading),
    ),
).collectAsLazyPagingItems()

@Composable
fun <T : Any> List<T>.asLazyPagingItemsWithError(
    refreshError: Throwable? = null,
    appendError: Throwable? = null,
) = flowOf(
    PagingData.from<T>(
        data = this,
        sourceLoadStates = loadStates(refreshError = refreshError, appendError = appendError),
    ),
).collectAsLazyPagingItems()
