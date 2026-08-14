package de.mistatee.erglog.data.remote.results

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.common.toHttpClient
import de.mistatee.erglog.data.model.ResultPage
import de.mistatee.erglog.data.remote.results.model.RemoteMetaResponse
import de.mistatee.erglog.data.remote.results.model.RemotePaginationResponse
import de.mistatee.erglog.data.remote.results.model.RemoteResultResponse
import de.mistatee.erglog.data.remote.results.model.RemoteResultsException
import de.mistatee.erglog.data.remote.results.model.RemoteResultsResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Concept2RemoteResultsDataSourceTest {
    @Test
    fun `fetchResults sends bearer token, page and per_page parameters`() =
        runTest {
            // given
            val page = 2
            val pageSize = 20
            var capturedUrl: String? = null
            var capturedAuthHeader: String? = null
            val mockEngine = MockEngine.Companion { request ->
                capturedUrl = request.url.toString()
                capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = MockData.Api.Results.resultsJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val httpClient = mockEngine.toHttpClient()
            val api = Concept2RemoteResultsDataSource(httpClient)

            // when
            api.fetchResults(accessToken = "access-token", page = page, pageSize = pageSize)

            // then
            val url = requireNotNull(capturedUrl)
            assertThat(capturedAuthHeader).isEqualTo("Bearer access-token")
            assertThat(url.contains("page=$page")).isTrue()
            assertThat(url.contains("per_page=$pageSize")).isTrue()
        }

    @Test
    fun `fetchResults maps a successful response to a ResultPage`() =
        runTest {
            // given
            val mockEngine = MockEngine.Companion {
                respond(
                    content = MockData.Api.Results.resultsJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val api = Concept2RemoteResultsDataSource(mockEngine.toHttpClient())

            // when
            val resultPage = api.fetchResults(accessToken = "access-token", page = 1, pageSize = 20)

            // then
            assertThat(resultPage.results.size).isEqualTo(1)
            assertThat(resultPage.results[0].id).isEqualTo(3L)
            assertThat(resultPage.currentPage).isEqualTo(1)
            assertThat(resultPage.totalPages).isEqualTo(1)
            assertThat(resultPage.hasNextPage).isFalse()
        }

    @Test
    fun `fetchResults throws ResultsApiException on non-200 response`() =
        runTest {
            // given
            val mockEngine = MockEngine.Companion {
                respond(
                    content = "",
                    status = HttpStatusCode.InternalServerError,
                )
            }
            val api = Concept2RemoteResultsDataSource(mockEngine.toHttpClient())

            // when
            val exception =
                runCatching { api.fetchResults(accessToken = "token", page = 1, pageSize = 20) }
                    .exceptionOrNull()

            // then
            assertThat(exception).isNotNull().isInstanceOf<RemoteResultsException>()
            assertThat((exception as RemoteResultsException).error).isEqualTo("http_500")
        }

    @Test
    fun `tenthsOfASecondToDuration converts to correct duration`() {
        // given: 152350 tenths of a second = 15235.0 seconds = 4:13:55.0
        val timeTenths = 152350L

        // when
        val duration = tenthsOfASecondToDuration(timeTenths)

        // then
        assertThat(duration).isEqualTo(4.hours + 13.minutes + 55.seconds)
    }

    @Test
    fun `parseResultDate parses Concept2 date string using the current system timezone`() {
        // given
        val date = "2013-06-21 00:00:00"

        // when
        val instant = parseResultDate(date)

        // then
        val expected = LocalDateTime(2013, 6, 21, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
        assertThat(instant).isEqualTo(expected)
    }

    @Test
    fun `toResultPage maps data and pagination fields`() {
        // given
        val mockResults = MockData.Api.Results.results
        val response =
            RemoteResultsResponse(
                data =
                    mockResults.map {
                        RemoteResultResponse(
                            id = it.id,
                            date = "2013-06-21 00:00:00",
                            distance = it.distance,
                            type = it.type,
                            time = 152350,
                        )
                    },
                meta =
                    RemoteMetaResponse(
                        pagination =
                            RemotePaginationResponse(
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
        assertThat(page.results.size).isEqualTo(mockResults.size)
        val first = page.results[0]
        assertThat(first.id).isEqualTo(mockResults[0].id)
        assertThat(first.type).isEqualTo(mockResults[0].type)
        assertThat(first.distance).isEqualTo(mockResults[0].distance)
        assertThat(first.duration).isEqualTo(4.hours + 13.minutes + 55.seconds)
        assertThat(first.date).isEqualTo(
            LocalDateTime(2013, 6, 21, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()),
        )
        assertThat(page.currentPage).isEqualTo(1)
        assertThat(page.totalPages).isEqualTo(1)
    }

    @Test
    fun `when current page is not the last page hasNextPage is true`() {
        // given
        val page = ResultPage(results = emptyList(), currentPage = 1, totalPages = 4)

        // when
        val hasNextPage = page.hasNextPage

        // then
        assertThat(hasNextPage).isTrue()
    }

    @Test
    fun `when current page is the last page hasNextPage is false`() {
        // given
        val page = ResultPage(results = emptyList(), currentPage = 4, totalPages = 4)

        // when
        val hasNextPage = page.hasNextPage

        // then
        assertThat(hasNextPage).isFalse()
    }
}
