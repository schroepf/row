package de.mistatee.erglog.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import de.mistatee.erglog.theme.ErgLogTheme
import de.mistatee.erglog.utils.asLazyPagingItems
import de.mistatee.erglog.utils.asLazyPagingItemsWithError
import kotlin.time.Duration
import kotlin.time.Instant

private class SampleResultsProvider : PreviewParameterProvider<List<Result>> {
    override val values =
        sequenceOf(
            listOf(
                Result(
                    id = 1,
                    date = Instant.parse("2013-06-21T00:00:00Z"),
                    distance = 23000,
                    type = "Rower",
                    duration = Duration.parse("4h13m55s"),
                ),
                Result(
                    id = 2,
                    date = Instant.parse("2013-06-19T00:00:00Z"),
                    distance = 5000,
                    type = "Rower",
                    duration = Duration.parse("21m4s"),
                ),
                Result(
                    id = 3,
                    date = Instant.parse("2013-06-17T00:00:00Z"),
                    distance = 2000,
                    type = "SkiErg",
                    duration = Duration.parse("7m32s"),
                ),
            ),
        )
}

@Preview(showBackground = true, name = "Loading finished")
@Composable
fun MainScreenPreview(
    @PreviewParameter(SampleResultsProvider::class) results: List<Result>,
) {
    ErgLogTheme {
        MainScreen(
            username = "Android",
            results = results.asLazyPagingItems(),
        )
    }
}

@Preview(showBackground = true, name = "Loading first page")
@Composable
fun MainScreenLoadingFirstPagePreview() {
    ErgLogTheme {
        MainScreen(
            username = "Android",
            results = emptyList<Result>().asLazyPagingItems(isLoading = true),
        )
    }
}

@Preview(showBackground = true, name = "Loading next page")
@Composable
fun MainScreenIsLoadingPreview(
    @PreviewParameter(SampleResultsProvider::class) results: List<Result>,
) {
    ErgLogTheme {
        MainScreen(
            username = "Android",
            results = results.asLazyPagingItems(isLoading = true),
        )
    }
}

@Preview(showBackground = true, name = "Next page error")
@Composable
fun MainScreenNextPageErrorPreview(
    @PreviewParameter(SampleResultsProvider::class) results: List<Result>,
) {
    ErgLogTheme {
        MainScreen(
            username = "Android",
            results = results.asLazyPagingItemsWithError(appendError = IllegalStateException("Network error")),
        )
    }
}

@Preview(showBackground = true, name = "First page error")
@Composable
fun MainScreenFirstPageErrorPreview() {
    ErgLogTheme {
        MainScreen(
            username = "Android",
            results = emptyList<Result>()
                .asLazyPagingItemsWithError(refreshError = IllegalStateException("Network error")),
        )
    }
}
