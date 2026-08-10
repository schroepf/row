package de.mistatee.erglog.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAuthRepositoryTest {
    @Test
    fun completeLogin_onSuccess_savesSession() =
        runTest {
            val sessionStore = FakeSessionStore()
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(exchangeResult = { SAMPLE_TOKEN_RESPONSE }),
                    sessionStore,
                )

            val result = repository.completeLogin("auth-code")

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_TOKEN_RESPONSE.accessToken, sessionStore.getSession()?.accessToken)
        }

    @Test
    fun completeLogin_onFailure_doesNotSaveSession() =
        runTest {
            val sessionStore = FakeSessionStore()
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(exchangeResult = { throw AuthException("invalid_grant", null) }),
                    sessionStore,
                )

            val result = repository.completeLogin("bad-code")

            assertTrue(result.isFailure)
            assertNull(sessionStore.getSession())
        }

    @Test
    fun refreshSession_onAuthFailure_clearsSession() =
        runTest {
            val sessionStore = FakeSessionStore().apply { saveSession(SAMPLE_SESSION) }
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(refreshResult = { throw AuthException("invalid_grant", null) }),
                    sessionStore,
                )

            val result = repository.refreshSession()

            assertTrue(result.isFailure)
            assertNull(sessionStore.getSession())
        }

    @Test
    fun currentSession_returnsStoredSession() =
        runTest {
            val sessionStore = FakeSessionStore().apply { saveSession(SAMPLE_SESSION) }
            val repository = DefaultAuthRepository(FakeAuthApi(), sessionStore)

            assertEquals(SAMPLE_SESSION, repository.currentSession())
        }

    @Test
    fun validSession_withFreshSession_returnsItWithoutRefreshing() =
        runTest {
            val freshSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().plusSeconds(3600))
            val sessionStore = FakeSessionStore().apply { saveSession(freshSession) }
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(refreshResult = { error("should not be called") }),
                    sessionStore,
                )

            val result = repository.validSession()

            assertTrue(result.isSuccess)
            assertEquals(freshSession, result.getOrNull())
        }

    @Test
    fun validSession_withStaleSession_refreshesAndReturnsNewSession() =
        runTest {
            val staleSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().minusSeconds(1))
            val sessionStore = FakeSessionStore().apply { saveSession(staleSession) }
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(refreshResult = { SAMPLE_TOKEN_RESPONSE }),
                    sessionStore,
                )

            val result = repository.validSession()

            assertTrue(result.isSuccess)
            assertEquals(SAMPLE_TOKEN_RESPONSE.accessToken, result.getOrNull()?.accessToken)
        }

    @Test
    fun validSession_withNoSession_failsWithNoSession() =
        runTest {
            val sessionStore = FakeSessionStore()
            val repository = DefaultAuthRepository(FakeAuthApi(), sessionStore)

            val result = repository.validSession()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SessionError.NoSession)
        }

    @Test
    fun validSession_whenRefreshFails_failsWithRefreshFailedWrappingCause() =
        runTest {
            val staleSession =
                SAMPLE_SESSION.copy(accessTokenExpiry = java.time.Instant.now().minusSeconds(1))
            val sessionStore = FakeSessionStore().apply { saveSession(staleSession) }
            val cause = AuthException("invalid_grant", null)
            val repository =
                DefaultAuthRepository(
                    FakeAuthApi(refreshResult = { throw cause }),
                    sessionStore,
                )

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

private class FakeAuthApi(
    private val exchangeResult: () -> TokenResponse = { error("not stubbed") },
    private val refreshResult: () -> TokenResponse = { error("not stubbed") },
) : AuthApi {
    override suspend fun exchangeAuthorizationCode(code: String): TokenResponse = exchangeResult()

    override suspend fun refreshAccessToken(refreshToken: String): TokenResponse = refreshResult()
}

private class FakeSessionStore : SessionStore {
    private var session: Session? = null

    override suspend fun getSession(): Session? = session

    override suspend fun saveSession(session: Session) {
        this.session = session
    }

    override suspend fun clearSession() {
        session = null
    }
}
