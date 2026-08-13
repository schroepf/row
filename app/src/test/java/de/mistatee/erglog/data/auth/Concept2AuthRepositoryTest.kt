package de.mistatee.erglog.data.auth

import de.mistatee.erglog.common.MockData
import de.mistatee.erglog.data.concept2.auth.Concept2AuthRepository
import de.mistatee.erglog.data.concept2.auth.api.AuthApi
import de.mistatee.erglog.data.concept2.auth.model.AuthException
import de.mistatee.erglog.data.concept2.auth.model.SessionError
import de.mistatee.erglog.data.concept2.auth.store.SessionStore
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class Concept2AuthRepositoryTest {
    @Test
    fun `completeLogin saves session on success`() = runTest {
        // given
        val authCode = "auth-code"
        val tokenResponse = MockData.Auth.tokenResponse
        val sessionStore = mockk<SessionStore> {
            coEvery { saveSession(any()) } just Runs
            coEvery { getSession() } just Awaits
        }

        val repository = Concept2AuthRepository(
            authApi = mockk {
                coEvery { exchangeAuthorizationCode(authCode) } returns tokenResponse
            },
            sessionStore = sessionStore,
            clock = MockData.clock,
        )

        // when
        val result = repository.completeLogin(authCode)

        // then
        assertTrue(result.isSuccess)
        coVerify {
            sessionStore.saveSession(
                withArg {
                    assertEquals(tokenResponse.accessToken, it.accessToken)
                    assertEquals(tokenResponse.refreshToken, it.refreshToken)
                },
            )
        }
    }

    @Test
    fun `completeLogin does not save session on failure`() = runTest {
        // Given
        val authCode = "bad-code"
        val sessionStore = mockk<SessionStore>()

        val repository = Concept2AuthRepository(
            authApi = mockk {
                coEvery { exchangeAuthorizationCode(authCode) } throws AuthException("invalid_grant", null)
            },
            sessionStore = sessionStore,
            clock = MockData.clock,
        )

        // When
        val result = repository.completeLogin(authCode)

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { sessionStore.saveSession(any()) }
    }

    @Test
    fun `refreshSession clears session on authentication failure`() = runTest {
        // Given
        val sessionStore = mockk<SessionStore> {
            coEvery { clearSession() } just Runs
            coEvery { getSession() } returns MockData.Auth.session
        }

        val repository = Concept2AuthRepository(
            authApi = mockk<AuthApi> {
                coEvery { refreshAccessToken(any()) } throws AuthException("invalid_grant", null)
            },
            sessionStore = sessionStore,
            clock = MockData.clock,
        )

        // When
        val result = repository.refreshSession()

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 1) { sessionStore.clearSession() }
    }

    @Test
    fun `currentSession returns stored session`() = runTest {
        // Given
        val session = MockData.Auth.session

        // When
        val repository = Concept2AuthRepository(
            authApi = mockk(),
            sessionStore = mockk {
                coEvery { getSession() } returns session
            },
            clock = MockData.clock,
        )

        // Then
        assertEquals(session, repository.currentSession())
    }

    @Test
    fun `validSession returns existing session if session is fresh`() = runTest {
        // Given
        val freshSession = MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().plus(3600.seconds))
        val authApi = mockk<AuthApi>()
        val repository = Concept2AuthRepository(
            authApi = authApi,
            sessionStore = mockk {
                coEvery { getSession() } returns freshSession
            },
            clock = MockData.clock,
        )

        // When
        val result = repository.validSession()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(freshSession, result.getOrNull())
        coVerify(exactly = 0) { authApi.refreshAccessToken(any()) }
    }

    @Test
    fun `validSession returns a fresh session when session is stale`() = runTest {
        // Given
        val staleSession = MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().minus(1.seconds))
        val tokenResponse = MockData.Auth.tokenResponse

        val sessionStore = mockk<SessionStore> {
            coEvery { getSession() } returns staleSession
            coEvery { saveSession(any()) } just Runs
        }
        val repository = Concept2AuthRepository(
            authApi = mockk {
                coEvery { refreshAccessToken(any()) } returns tokenResponse
            },
            sessionStore = sessionStore,
            clock = MockData.clock,
        )

        // When
        val result = repository.validSession()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(tokenResponse.accessToken, result.getOrNull()?.accessToken)
    }

    @Test
    fun `validSession returns NoSession error on missing session`() = runTest {
        // Given
        val repository = Concept2AuthRepository(
            authApi = mockk(),
            sessionStore = mockk {
                coEvery { getSession() } returns null
            },
            clock = MockData.clock,
        )

        // When
        val result = repository.validSession()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SessionError.NoSession)
    }

    @Test
    fun `validSession returns RefreshFailed error when refresh fails`() = runTest {
        // Given
        val staleSession = MockData.Auth.session.copy(accessTokenExpiry = MockData.clock.now().minus(1.seconds))
        val cause = AuthException("invalid_grant", null)

        val repository = Concept2AuthRepository(
            authApi = mockk {
                coEvery { refreshAccessToken(any()) } throws cause
            },
            sessionStore = mockk {
                coEvery { getSession() } returns staleSession
                coEvery { clearSession() } returns Unit
            },
            clock = MockData.clock,
        )

        // When
        val result = repository.validSession()

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is SessionError.RefreshFailed)
        assertEquals(cause, error?.cause)
    }
}
