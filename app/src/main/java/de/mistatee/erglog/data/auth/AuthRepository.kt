package de.mistatee.erglog.data.auth

import java.time.Instant

/**
 * How close to expiry an Access Token must be before [AuthRepository.validSession] proactively
 * refreshes it rather than returning it as-is. Adjust here if it needs tuning.
 */
private const val ACCESS_TOKEN_EXPIRY_BUFFER_SECONDS = 60L

interface AuthRepository {
    /** The current [Session], or `null` if the Logbook Account holder is not authenticated. */
    suspend fun currentSession(): Session?

    /**
     * Exchanges an Authorization Code (from the OAuth redirect) for an Access Token and Refresh
     * Token, and saves the resulting [Session].
     */
    suspend fun completeLogin(code: String): Result<Unit>

    /**
     * Exchanges the current Session's Refresh Token for a new Access Token. Clears the Session
     * if the Refresh Token has been rejected (e.g. revoked or expired), per the app's
     * auth-failure handling rule.
     */
    suspend fun refreshSession(): Result<Session>

    /**
     * Returns a [Session] guaranteed to have a live Access Token, refreshing it first if it's
     * missing, expired, or about to expire. Fails with [SessionError.NoSession] if the Logbook
     * Account holder isn't authenticated, or [SessionError.RefreshFailed] if a needed refresh
     * fails.
     */
    suspend fun validSession(): Result<Session>

    /** Clears the current Session, ending it locally without contacting Concept2. */
    suspend fun logout()
}

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) : AuthRepository {
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
        val refreshThreshold = Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRY_BUFFER_SECONDS)
        return when {
            session == null -> Result.failure(SessionError.NoSession)
            session.accessTokenExpiry.isAfter(refreshThreshold) -> Result.success(session)
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
            accessTokenExpiry = Instant.now().plusSeconds(expiresIn),
        )
}
