package de.mistatee.erglog.data.results

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.auth.model.SessionError
import de.mistatee.erglog.data.concept2.logbook.results.Concept2ResultRepository
import de.mistatee.erglog.data.concept2.logbook.results.api.ResultsApi
import de.mistatee.erglog.data.concept2.logbook.results.model.ResultsApiException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class Concept2ResultRepositoryTest {
    @Test
    fun `fetchPage returns results page on success`() =
        runTest {
            // given
            val session = MockData.Auth.session
            val page = 1
            val pageSize = 20
            val resultsPage = MockData.Api.Results.resultsPage

            val repository = Concept2ResultRepository(
                authRepository = mockk {
                    coEvery { validSession() } returns Result.success(session)
                },
                resultsApi = mockk {
                    coEvery {
                        fetchResults(
                            accessToken = session.accessToken,
                            page = page,
                            pageSize = pageSize,
                        )
                    } returns resultsPage
                },
            )

            // when
            val result = repository.fetchPage(page = page, pageSize = pageSize)

            // then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(resultsPage)
        }

    @Test
    fun `fetchPage returns failure on missing session`() =
        runTest {
            // given
            val repository = Concept2ResultRepository(
                authRepository = mockk {
                    coEvery { validSession() } returns Result.failure(SessionError.NoSession)
                },
                resultsApi = mockk(),
            )

            // when
            val result = repository.fetchPage(page = 1, pageSize = 20)

            // then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<SessionError.NoSession>()
        }

    @Test
    fun `fetchPage returns failure when API throws`() =
        runTest {
            // give
            val page = 1
            val pageSize = 20
            val session = MockData.Auth.session
            val apiFailure = ResultsApiException("http_500", null)

            val repository = Concept2ResultRepository(
                authRepository = mockk {
                    coEvery { validSession() } returns Result.success(session)
                },
                resultsApi = mockk<ResultsApi> {
                    coEvery { fetchResults(session.accessToken, page = page, pageSize = pageSize) } throws apiFailure
                },
            )

            // when
            val result = repository.fetchPage(page = page, pageSize = pageSize)

            // then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isEqualTo(apiFailure)
        }
}
