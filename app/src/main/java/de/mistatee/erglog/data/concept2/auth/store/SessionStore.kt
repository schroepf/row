package de.mistatee.erglog.data.concept2.auth.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.mistatee.erglog.data.concept2.auth.model.Session

/** Persists the current [Session] in an encrypted, app-private preferences store. */
interface SessionStore {
    suspend fun getSession(): Session?

    suspend fun saveSession(session: Session)

    suspend fun clearSession()
}

