package de.mistatee.erglog.data.results

import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.logbook.results.api.Concept2ResultsApi
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultsApiException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Concept2ResultsApiTest {
    @Test
    fun `fetchResults sends bearer token, page and per_page parameters`() =
        runTest {
            // given
            val page = 2
            val pageSize = 20
            var capturedUrl: String? = null
            var capturedAuthHeader: String? = null
            val mockEngine = MockEngine { request ->
                capturedUrl = request.url.toString()
                capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                respond(
                    content = MockData.Api.Results.resultsJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val httpClient = mockEngine.toHttpClient()
            val api = Concept2ResultsApi(httpClient)

            // when
            api.fetchResults(accessToken = "access-token", page = page, pageSize = pageSize)

            // then
            val url = requireNotNull(capturedUrl)
            assertEquals("Bearer access-token", capturedAuthHeader)
            assertTrue(url.contains("page=$page"))
            assertTrue(url.contains("per_page=$pageSize"))
        }

    @Test
    fun `fetchResults maps a successful response to a ResultPage`() =
        runTest {
            // given
            val mockEngine = MockEngine {
                respond(
                    content = MockData.Api.Results.resultsJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val api = Concept2ResultsApi(mockEngine.toHttpClient())

            // when
            val resultPage = api.fetchResults(accessToken = "access-token", page = 1, pageSize = 20)

            // then
            assertEquals(1, resultPage.results.size)
            assertEquals(3L, resultPage.results[0].id)
            assertEquals(1, resultPage.currentPage)
            assertEquals(1, resultPage.totalPages)
            assertFalse(resultPage.hasNextPage)
        }

    @Test
    fun `fetchResults throws ResultsApiException on non-200 response`() =
        runTest {
            // given
            val mockEngine = MockEngine {
                respond(
                    content = "",
                    status = HttpStatusCode.InternalServerError,
                )
            }
            val api = Concept2ResultsApi(mockEngine.toHttpClient())

            // when
            val exception = runCatching { api.fetchResults(accessToken = "token", page = 1, pageSize = 20) }
                .exceptionOrNull()

            // then
            assertTrue(exception is ResultsApiException)
            assertEquals("http_500", (exception as ResultsApiException).error)
        }
}

private fun MockEngine.toHttpClient(): HttpClient =
    HttpClient(this) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
