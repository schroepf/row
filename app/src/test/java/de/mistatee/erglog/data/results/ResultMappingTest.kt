package de.mistatee.erglog.data.results

import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.logbook.results.model.MetaResponse
import de.mistatee.erglog.data.concept2.logbook.results.model.PaginationResponse
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultResponse
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultsResponse
import de.mistatee.erglog.data.concept2.logbook.results.model.parseResultDate
import de.mistatee.erglog.data.concept2.logbook.results.model.tenthsOfASecondToDuration
import de.mistatee.erglog.data.concept2.logbook.results.model.toResultPage
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ResultMappingTest {
    @Test
    fun `tenthsOfASecondToDuration converts to correct duration`() {
        // given: 152350 tenths of a second = 15235.0 seconds = 4:13:55.0
        val timeTenths = 152350L

        // when
        val duration = tenthsOfASecondToDuration(timeTenths)

        // then
        assertEquals(4.hours + 13.minutes + 55.seconds, duration)
    }

    @Test
    fun `parseResultDate parses Concept2 date string using the current system timezone`() {
        // given
        val date = "2013-06-21 00:00:00"

        // when
        val instant = parseResultDate(date)

        // then
        val expected = LocalDateTime(2013, 6, 21, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
        assertEquals(expected, instant)
    }

    @Test
    fun `toResultPage maps data and pagination fields`() {
        // given
        val mockResults = MockData.Api.Results.results
        val response =
            ResultsResponse(
                data =
                    mockResults.map {
                        ResultResponse(
                            id = it.id,
                            date = "2013-06-21 00:00:00",
                            distance = it.distance,
                            type = it.type,
                            time = 152350,
                        )
                    },
                meta =
                    MetaResponse(
                        pagination =
                            PaginationResponse(
                                total = 9,
                                count = 9,
                                perPage = 50,
                                currentPage = 1,
                                totalPages = 1,
                            ),
                    ),
            )

        // when
        val page = response.toResultPage()

        // then
        assertEquals(mockResults.size, page.results.size)
        val first = page.results[0]
        assertEquals(mockResults[0].id, first.id)
        assertEquals(mockResults[0].type, first.type)
        assertEquals(mockResults[0].distance, first.distance)
        assertEquals(4.hours + 13.minutes + 55.seconds, first.duration)
        assertEquals(
            LocalDateTime(2013, 6, 21, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()),
            first.date,
        )
        assertEquals(1, page.currentPage)
        assertEquals(1, page.totalPages)
    }

    @Test
    fun `when current page is not the last page hasNextPage is true`() {
        // given
        val page = ResultPage(results = emptyList(), currentPage = 1, totalPages = 4)

        // when
        val hasNextPage = page.hasNextPage

        // then
        assertTrue(hasNextPage)
    }

    @Test
    fun `when current page is the last page hasNextPage is false`() {
        // given
        val page = ResultPage(results = emptyList(), currentPage = 4, totalPages = 4)

        // when
        val hasNextPage = page.hasNextPage

        // then
        assertFalse(hasNextPage)
    }
}
