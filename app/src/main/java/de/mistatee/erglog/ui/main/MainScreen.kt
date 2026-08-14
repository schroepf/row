package de.mistatee.erglog.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.mistatee.erglog.data.model.Result
import de.mistatee.erglog.utils.formatResultDate
import de.mistatee.erglog.utils.formatResultDuration

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val current = state) {
        MainScreenUiState.Loading -> {
            // Blank
        }

        is MainScreenUiState.Success -> {
            val results = viewModel.resultsFlow.collectAsLazyPagingItems()
            val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
            // Stale (see CONTEXT.md) if either the Profile refresh or the Result History sync
            // most recently failed — either one leaves some part of the screen showing cached data.
            val isStale = current.syncError != null || results.loadState.refresh is LoadState.Error
            MainScreen(
                username = current.username,
                results = results,
                modifier = modifier,
                isStale = isStale,
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.refresh()
                    results.refresh()
                },
            )
        }

        is MainScreenUiState.Error -> {
            Text("Error loading data: ${current.throwable.message}")
        }
    }
}

@Suppress("LongParameterList") // Compose params with defaults; all but the first two are optional.
@Composable
internal fun MainScreen(
    username: String,
    results: LazyPagingItems<Result>,
    modifier: Modifier = Modifier,
    isStale: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "profileheader") { ProfileHeader(username, modifier = Modifier.padding(24.dp)) }

            if (isStale) {
                item(key = "stalebanner") { StaleDataBanner() }
            }

            item(key = "resulthistoryheader") { ResultHistoryHeader() }

            items(count = results.itemCount, key = results.itemKey { it.id }) { index ->
                val result = results[index]
                if (result != null) {
                    ResultHistoryItem(result)
                    if (index != results.itemCount - 1) {
                        HorizontalDivider()
                    }
                }
            }

            item(key = "footer") {
                ResultHistoryFooter(loadState = results.loadState, onRetry = results::retry)
            }
        }
    }
}

@Composable
private fun ProfileHeader(username: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $username!",
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier,
    )
}

/**
 * Subtle, non-blocking hint that the currently shown data is Stale (see CONTEXT.md) — a background
 * refresh failed, but the cached username and Result History are still worth showing underneath.
 */
@Composable
private fun StaleDataBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Showing saved data — couldn't refresh",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultHistoryHeader(modifier: Modifier = Modifier) {
    Text(
        text = "Result History",
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ResultHistoryItem(result: Result, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = result.type, style = MaterialTheme.typography.titleMedium)
            Text(
                text = result.date.formatResultDate(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${result.distance}m", style = MaterialTheme.typography.titleMedium)
            Text(
                text = result.duration.formatResultDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultHistoryFooter(
    loadState: CombinedLoadStates,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (isLoading, errorMessage) = when {
        loadState.refresh is LoadState.Loading -> true to null
        loadState.refresh is LoadState.Error -> false to "Couldn't load your result history."
        loadState.append is LoadState.Loading -> true to null
        loadState.append is LoadState.Error -> false to "Couldn't load the next page."
        else -> false to null
    }

    when {
        errorMessage != null -> InlineError(message = errorMessage, onRetry = onRetry, modifier = modifier)
        isLoading -> {
            Box(
                modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }

        else -> {
            // Idle / end of pagination reached: nothing to show.
        }
    }
}

@Composable
private fun InlineError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
