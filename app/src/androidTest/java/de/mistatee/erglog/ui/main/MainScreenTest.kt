package de.mistatee.erglog.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import de.mistatee.erglog.data.concept2.logbook.results.model.Result
import de.mistatee.erglog.utils.formatResultDate
import de.mistatee.erglog.utils.formatResultDuration
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Instant


/** UI tests for [de.mistatee.erglog.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setContent(
        username: String = "Tester",
        results: List<Result> = emptyList(),
    ) {
        composeTestRule.setContent {
            val pagingItems = flowOf(PagingData.from(results)).collectAsLazyPagingItems()
            MainScreen(
                username = username,
                results = pagingItems,
            )
        }
    }

    @Test
    fun username_exists() {
        val username = "John Doe"
        setContent(username = username)
        composeTestRule.onNodeWithText("Hello $username!").assertExists()
    }

    @Test
    fun result_withHoursInDuration_displaysTypeDistanceDateAndDuration() {
        val result =
            Result(
                id = 1,
                date = Instant.parse("2013-06-21T00:00:00Z"),
                distance = 23_000,
                type = "Rower",
                duration = Duration.parse("4h13m55s"),
            )

        setContent(results = listOf(result))

        composeTestRule.onNodeWithText(result.type).assertExists()
        composeTestRule.onNodeWithText(result.date.formatResultDate()).assertExists()
        composeTestRule.onNodeWithText("${result.distance}m").assertExists()
        composeTestRule.onNodeWithText(result.duration.formatResultDuration()).assertExists()
    }

    @Test
    fun result_withoutHoursInDuration_displaysDurationWithoutHoursSegment() {
        val result =
            Result(
                id = 2,
                date = Instant.parse("2013-06-19T00:00:00Z"),
                distance = 5_000,
                type = "Rower",
                duration = Duration.parse("21m4s"),
            )

        setContent(results = listOf(result))

        composeTestRule.onNodeWithText("21:04").assertExists()
    }

    @Test
    fun multipleResults_allAreDisplayed() {
        val results =
            listOf(
                Result(
                    id = 1,
                    date = Instant.parse("2013-06-21T00:00:00Z"),
                    distance = 23_000,
                    type = "Rower",
                    duration = Duration.parse("4h13m55s"),
                ),
                Result(
                    id = 2,
                    date = Instant.parse("2013-06-19T00:00:00Z"),
                    distance = 5_000,
                    type = "Rower",
                    duration = Duration.parse("21m4s"),
                ),
                Result(
                    id = 3,
                    date = Instant.parse("2013-06-17T00:00:00Z"),
                    distance = 2_000,
                    type = "SkiErg",
                    duration = Duration.parse("7m32s"),
                ),
            )

        setContent(results = results)

        composeTestRule.onNodeWithText("23000m").assertExists()
        composeTestRule.onNodeWithText("5000m").assertExists()
        composeTestRule.onNodeWithText("2000m").assertExists()
        composeTestRule.onNodeWithText("SkiErg").assertExists()
    }

    @Test
    fun noResults_noResultRowIsDisplayed() {
        setContent(results = emptyList())

        composeTestRule.onNodeWithText("Rower").assertDoesNotExist()
        composeTestRule.onNodeWithText("SkiErg").assertDoesNotExist()
    }
}

