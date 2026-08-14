package de.mistatee.erglog.data.local

import de.mistatee.erglog.data.model.Session

/** Persists the current [Session] in an encrypted, app-private preferences store. */
interface PreferencesStore {
    suspend fun getSession(): Session?

    suspend fun saveSession(session: Session)

    suspend fun clearSession()
}

