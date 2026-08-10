package de.mistatee.erglog.data.auth

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Concept2AuthRepositoryTest {
    @Test
    fun completeLogin_onSuccess_savesSession() =
        runTest {
            val sessionStore = mockk<SessionStore>()
            var savedSession: Session? = null
            coEvery { sessionStore.saveSession(any()) } answers { savedSession = firstArg() }
            coEvery { sessionStore.getSession() } answers { savedSession }
            val authApi = mockk<AuthApi>()
            coEvery { authApi.exchangeAuthorizationCode("auth-code") } returns SAMPLE_TOKEN_RESPONSE
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.completeLogin("auth-code")

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_TOKEN_RESPONSE.accessToken, sessionStore.getSession()?.accessToken)
        }

    @Test
    fun completeLogin_onFailure_doesNotSaveSession() =
        runTest {
            val sessionStore = mockk<SessionStore>()
            coEvery { sessionStore.getSession() } returns null
            val authApi = mockk<AuthApi>()
            coEvery { authApi.exchangeAuthorizationCode("bad-code") } throws AuthException("invalid_grant", null)
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.completeLogin("bad-code")

            assertTrue(result.isFailure)
            assertNull(sessionStore.getSession())
        }

    @Test
    fun refreshSession_onAuthFailure_clearsSession() =
        runTest {
            val sessionStore = mockk<SessionStore>()
            var currentSession: Session? = SAMPLE_SESSION
            coEvery { sessionStore.getSession() } answers { currentSession }
            coEvery { sessionStore.clearSession() } answers { currentSession = null }
            val authApi = mockk<AuthApi>()
            coEvery { authApi.refreshAccessToken(any()) } throws AuthException("invalid_grant", null)
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.refreshSession()

            assertTrue(result.isFailure)
            assertNull(sessionStore.getSession())
        }

    @Test
    fun currentSession_returnsStoredSession() =
        runTest {
            val sessionStore = mockk<SessionStore>()
            coEvery { sessionStore.getSession() } returns SAMPLE_SESSION
            val authApi = mockk<AuthApi>()
            val repository = Concept2AuthRepository(authApi, sessionStore)

            assertEquals(SAMPLE_SESSION, repository.currentSession())
        }

    @Test
    fun validSession_withFreshSession_returnsItWithoutRefreshing() =
        runTest {
            val freshSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().plusSeconds(3600))
            val sessionStore = mockk<SessionStore>()
            coEvery { sessionStore.getSession() } returns freshSession
            val authApi = mockk<AuthApi>()
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.validSession()

            assertTrue(result.isSuccess)
            assertEquals(freshSession, result.getOrNull())
            coVerify(exactly = 0) { authApi.refreshAccessToken(any()) }
        }

    @Test
    fun validSession_withStaleSession_refreshesAndReturnsNewSession() =
        runTest {
            val staleSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().minusSeconds(1))
            val sessionStore = mockk<SessionStore>()
            var currentSession: Session? = staleSession
            coEvery { sessionStore.getSession() } answers { currentSession }
            coEvery { sessionStore.saveSession(any()) } answers { currentSession = firstArg() }
            val authApi = mockk<AuthApi>()
            coEvery { authApi.refreshAccessToken(any()) } returns SAMPLE_TOKEN_RESPONSE
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.validSession()

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_TOKEN_RESPONSE.accessToken, result.getOrNull()?.accessToken)
        }

    @Test
    fun validSession_withNoSession_failsWithNoSession() =
        runTest {
            val sessionStore = mockk<SessionStore>()
            coEvery { sessionStore.getSession() } returns null
            val authApi = mockk<AuthApi>()
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.validSession()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SessionError.NoSession)
        }

    @Test
    fun validSession_whenRefreshFails_failsWithRefreshFailedWrappingCause() =
        runTest {
            val staleSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().minusSeconds(1))
            val sessionStore = mockk<SessionStore>()
            coEvery { sessionStore.getSession() } returns staleSession
            coEvery { sessionStore.clearSession() } returns Unit
            val cause = AuthException("invalid_grant", null)
            val authApi = mockk<AuthApi>()
            coEvery { authApi.refreshAccessToken(any()) } throws cause
            val repository = Concept2AuthRepository(authApi, sessionStore)

            val result = repository.validSession()

            assertTrue(result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue(error is SessionError.RefreshFailed)
            assertEquals(cause, error?.cause)
        }

    private companion object {
        val SAMPLE_TOKEN_RESPONSE =
            TokenResponse(
                accessToken = "access-token",
                tokenType = "Bearer",
                expiresIn = 604_800,
                refreshToken = "refresh-token",
            )
        val SAMPLE_SESSION =
            Session(
                accessToken = "existing-access-token",
                refreshToken = "existing-refresh-token",
                accessTokenExpiry = java.time.Instant.EPOCH,
            )
    }
}
