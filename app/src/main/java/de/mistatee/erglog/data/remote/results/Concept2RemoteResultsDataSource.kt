package de.mistatee.erglog.data.remote.results

import de.mistatee.erglog.data.model.Result
import de.mistatee.erglog.data.model.ResultPage
import de.mistatee.erglog.data.remote.results.model.RemoteResultResponse
import de.mistatee.erglog.data.remote.results.model.RemoteResultsException
import de.mistatee.erglog.data.remote.results.model.RemoteResultsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private const val RESULTS_URL = "https://log.concept2.com/api/users/me/results"

class Concept2RemoteResultsDataSource(
    private val httpClient: HttpClient,
) : RemoteResultsDataSource {
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
            throw RemoteResultsException(
                error = "http_${response.status.value}",
                errorDescription = null,
            )
        }
        return response.body<RemoteResultsResponse>().toResultPage()
    }
}

private const val TENTHS_TO_MILLIS = 100L

/**
 * Converts the raw `time` field (tenths of a second) from the Concept2 API into a [Duration].
 * Uses integer arithmetic to avoid floating point rounding.
 */
internal fun tenthsOfASecondToDuration(timeTenths: Long): Duration = (timeTenths * TENTHS_TO_MILLIS).milliseconds

/**
 * Parses a Concept2 `date` string (e.g. "2013-06-21 00:00:00") into an [Instant]. The API does
 * not provide timezone information, so the string is treated as a local date/time in the
 * device's current timezone. This must stay consistent with [de.mistatee.erglog.utils.formatResultDate],
 * which formats using [TimeZone.currentSystemDefault] as well, so that the calendar date shown to
 * the user always matches the calendar date encoded in the raw API string.
 */
internal fun parseResultDate(date: String): Instant {
    val isoLocalDateTime = date.replaceFirst(' ', 'T')
    return LocalDateTime.parse(isoLocalDateTime).toInstant(TimeZone.currentSystemDefault())
}

/** Maps this [RemoteResultsResponse] to a domain [ResultPage]. */
internal fun RemoteResultsResponse.toResultPage(): ResultPage =
    ResultPage(
        results = data.map { it.toResult() },
        currentPage = meta.pagination.currentPage,
        totalPages = meta.pagination.totalPages,
    )

private fun RemoteResultResponse.toResult(): Result =
    Result(
        id = id,
        date = parseResultDate(date),
        distance = distance,
        type = type,
        duration = tenthsOfASecondToDuration(time),
    )
