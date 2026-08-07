package de.mistatee.erglog.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.time.Instant

private const val PREFS_FILE_NAME = "de.mistatee.erglog.data.auth.session"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_ACCESS_TOKEN_EXPIRY_EPOCH_SECONDS = "access_token_expiry_epoch_seconds"

/** Persists the current [Session] in an encrypted, app-private preferences file. */
interface SessionStore {
    fun getSession(): Session?

    fun saveSession(session: Session)

    fun clearSession()
}

class EncryptedSessionStore(context: Context) : SessionStore {
    private val preferences: SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    override fun getSession(): Session? {
        val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)
        val expiryEpochSeconds = preferences.getLong(KEY_ACCESS_TOKEN_EXPIRY_EPOCH_SECONDS, -1)
        val hasCompleteSession = accessToken != null && refreshToken != null && expiryEpochSeconds >= 0
        return if (hasCompleteSession) {
            Session(
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessTokenExpiry = Instant.ofEpochSecond(expiryEpochSeconds),
            )
        } else {
            null
        }
    }

    override fun saveSession(session: Session) {
        preferences.edit {
            putString(KEY_ACCESS_TOKEN, session.accessToken)
            putString(KEY_REFRESH_TOKEN, session.refreshToken)
            putLong(KEY_ACCESS_TOKEN_EXPIRY_EPOCH_SECONDS, session.accessTokenExpiry.epochSecond)
        }
    }

    override fun clearSession() {
        preferences.edit { clear() }
    }
}
