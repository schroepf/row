package de.mistatee.erglog.data.concept2.auth

import de.mistatee.erglog.data.concept2.auth.api.AuthApi
import de.mistatee.erglog.data.concept2.auth.model.AuthException
import de.mistatee.erglog.data.concept2.auth.model.Session
import de.mistatee.erglog.data.concept2.auth.model.SessionError
import de.mistatee.erglog.data.concept2.auth.model.TokenResponse
import de.mistatee.erglog.data.concept2.auth.store.SessionStore
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val ACCESS_TOKEN_EXPIRY_BUFFER = 60.seconds

class Concept2AuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
    private val clock: Clock,
) : AuthRepository {
    override val authorizationUrl: String
        get() = authApi.authorizationUrl

    override suspend fun currentSession(): Session? = sessionStore.getSession()

    override suspend fun completeLogin(code: String): Result<Unit> =
        runCatching {
            val token = authApi.exchangeAuthorizationCode(code)
            sessionStore.saveSession(token.toSession())
        }

    override suspend fun refreshSession(): Result<Session> {
        val session = sessionStore.getSession() ?: return Result.failure(IllegalStateException("No session"))
        return runCatching {
            authApi.refreshAccessToken(session.refreshToken).toSession()
        }.onSuccess {
            sessionStore.saveSession(it)
        }.onFailure { failure ->
            if (failure is AuthException) {
                sessionStore.clearSession()
            }
        }
    }

    override suspend fun validSession(): Result<Session> {
        val session = sessionStore.getSession()
        val refreshThreshold = clock.now().plus(ACCESS_TOKEN_EXPIRY_BUFFER)
        return when {
            session == null -> Result.failure(SessionError.NoSession)
            session.accessTokenExpiry > refreshThreshold -> Result.success(session)
            else ->
                refreshSession().fold(
                    onSuccess = { Result.success(it) },
                    onFailure = { Result.failure(SessionError.RefreshFailed(it)) },
                )
        }
    }

    override suspend fun logout() {
        sessionStore.clearSession()
    }

    private fun TokenResponse.toSession() =
        Session(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = clock.now().plus(expiresIn.seconds),
        )
}
