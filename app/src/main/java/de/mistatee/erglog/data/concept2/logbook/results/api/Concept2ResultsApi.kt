package de.mistatee.erglog.data.concept2.logbook.results.api

import de.mistatee.erglog.data.concept2.logbook.results.model.ResultPage
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultsApiException
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultsResponse
import de.mistatee.erglog.data.concept2.logbook.results.model.toResultPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

private const val RESULTS_URL = "https://log.concept2.com/api/users/me/results"

class Concept2ResultsApi(
    private val httpClient: HttpClient,
) : ResultsApi {
    override suspend fun fetchResults(
        accessToken: String,
        page: Int,
        pageSize: Int,
    ): ResultPage {
        val response =
            httpClient.get(RESULTS_URL) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                parameter("page", page)
                parameter("per_page", pageSize)
            }
        if (response.status != HttpStatusCode.OK) {
            // There's no clean documented error shape for GET /api/users/me/results; fall back
            // to a generic error keyed on the HTTP status.
            throw ResultsApiException(
                error = "http_${response.status.value}",
                errorDescription = null,
            )
        }
        return response.body<ResultsResponse>().toResultPage()
    }
}
