package de.mistatee.erglog.common

import de.mistatee.erglog.data.model.Profile
import de.mistatee.erglog.data.model.Result
import de.mistatee.erglog.data.model.ResultPage
import de.mistatee.erglog.data.model.Session
import de.mistatee.erglog.data.remote.auth.model.RemoteTokenResponse
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

object MockData {
    val clock = mockk<kotlin.time.Clock> {
        every { now() } answers { Instant.parse("2026-08-21T12:00:00.000Z") }
    }

    data object Auth {
        val remoteTokenResponse = RemoteTokenResponse(
            accessToken = "response-access-token",
            tokenType = "Bearer",
            expiresIn = 604_800,
            refreshToken = "response-refresh-token",
        )

        val session = Session(
            accessToken = "session-access-token",
            refreshToken = "session-refresh-token",
            accessTokenExpiry = clock.now().plus(3600.seconds),
        )
    }

    data object Api {
        data object Profile {
            val profile = Profile(username = "rower1")
        }

        data object Results {
            val resultsJson = """
                {
                    "data": [
                        {
                            "id": 3,
                            "date": "2013-06-21 00:00:00",
                            "distance": 23000,
                            "type": "rower",
                            "time": 152350
                        }
                    ],
                    "meta": {
                        "pagination": {
                            "total": 9,
                            "count": 9,
                            "per_page": 20,
                            "current_page": 1,
                            "total_pages": 1
                        }
                    }
                }
            """.trimIndent()

            val results = listOf(
                Result(
                    id = 1L,
                    date = Instant.parse("2013-06-21T00:00:00Z"),
                    distance = 2000,
                    type = "Rower",
                    duration = 8.minutes,
                ),
                Result(
                    id = 2L,
                    date = Instant.parse("2013-06-22T00:00:00Z"),
                    distance = 5000,
                    type = "Rower",
                    duration = 21.minutes,
                ),
            )

            val resultsPage = ResultPage(
                results = emptyList(),
                currentPage = 1,
                totalPages = 1,
            )
        }
    }
}
