package de.mistatee.erglog.data.concept2.logbook.results.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private const val TENTHS_TO_MILLIS = 100L

/**
 * Converts the raw `time` field (tenths of a second) from the Concept2 API into a [Duration].
 * Uses integer arithmetic to avoid floating point rounding.
 */
fun tenthsOfASecondToDuration(timeTenths: Long): Duration = (timeTenths * TENTHS_TO_MILLIS).milliseconds

/**
 * Parses a Concept2 `date` string (e.g. "2013-06-21 00:00:00") into an [Instant]. The API does
 * not provide timezone information, so the string is treated as a local date/time in the
 * device's current timezone. This must stay consistent with [de.mistatee.erglog.utils.formatResultDate],
 * which formats using [TimeZone.currentSystemDefault] as well, so that the calendar date shown to
 * the user always matches the calendar date encoded in the raw API string.
 */
fun parseResultDate(date: String): Instant {
    val isoLocalDateTime = date.replaceFirst(' ', 'T')
    return LocalDateTime.parse(isoLocalDateTime).toInstant(TimeZone.currentSystemDefault())
}

/** Maps this [ResultsResponse] to a domain [ResultPage]. */
fun ResultsResponse.toResultPage(): ResultPage =
    ResultPage(
        results = data.map { it.toResult() },
        currentPage = meta.pagination.currentPage,
        totalPages = meta.pagination.totalPages,
    )

private fun ResultResponse.toResult(): Result =
    Result(
        id = id,
        date = parseResultDate(date),
        distance = distance,
        type = type,
        duration = tenthsOfASecondToDuration(time),
    )
