package de.mistatee.erglog.data.repository

import de.mistatee.erglog.data.local.LocalCache
import de.mistatee.erglog.data.local.PreferencesStore
import de.mistatee.erglog.data.model.Session
import de.mistatee.erglog.data.model.SessionError
import de.mistatee.erglog.data.remote.auth.RemoteAuthDataSource
import de.mistatee.erglog.data.remote.auth.model.RemoteAuthException
import de.mistatee.erglog.data.remote.auth.model.RemoteTokenResponse
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val ACCESS_TOKEN_EXPIRY_BUFFER = 60.seconds

class Concept2AuthRepository(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val preferencesStore: PreferencesStore,
    private val clock: Clock,
    private val localCache: LocalCache,
) : AuthRepository {
    override val authorizationUrl: String
        get() = remoteAuthDataSource.authorizationUrl

    override suspend fun currentSession(): Session? = preferencesStore.getSession()

    override suspend fun completeLogin(code: String): Result<Unit> =
        runCatching {
            val token = remoteAuthDataSource.exchangeAuthorizationCode(code)
            preferencesStore.saveSession(token.toSession())
        }

    override suspend fun refreshSession(): Result<Session> {
        val session = preferencesStore.getSession() ?: return Result.failure(IllegalStateException("No session"))
        return runCatching {
            remoteAuthDataSource.refreshAccessToken(session.refreshToken).toSession()
        }.onSuccess {
            preferencesStore.saveSession(it)
        }.onFailure { failure ->
            if (failure is RemoteAuthException) {
                preferencesStore.clearSession()
            }
        }
    }

    override suspend fun validSession(): Result<Session> {
        val session = preferencesStore.getSession()
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
        preferencesStore.clearSession()
        localCache.clear()
    }

    private fun RemoteTokenResponse.toSession() =
        Session(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiry = clock.now().plus(expiresIn.seconds),
        )
}
