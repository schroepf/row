package de.mistatee.erglog.data.repository

import de.mistatee.erglog.data.model.Session

interface AuthRepository {
    val authorizationUrl: String

    /**
     *  The current [Session], or `null` if the Logbook Account holder is
     *  not authenticated.
     */
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
     * Returns a [Session] guaranteed to have a live Access Token, refreshing it first if it's missing, expired, or
     * about to expire. Fails with [de.mistatee.erglog.data.model.SessionError.NoSession] if the Logbook
     * Account holder isn't authenticated, or [de.mistatee.erglog.data.model.SessionError.RefreshFailed]
     * if a needed refresh fails.
     */
    suspend fun validSession(): Result<Session>

    /** Clears the current Session, ending it locally without contacting Concept2. */
    suspend fun logout()
}

