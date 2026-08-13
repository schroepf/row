package de.mistatee.erglog.data.concept2.auth.store

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import de.mistatee.erglog.data.concept2.auth.model.Session
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

private const val KEYSET_NAME = "de.mistatee.erglog.session_keyset"
private const val KEYSET_PREFS_FILE_NAME = "de.mistatee.erglog.session_keyset_prefs"
private const val MASTER_KEY_URI = "android-keystore://de.mistatee.erglog.session_master_key"

private val KEY_ACCESS_TOKEN_CIPHER = stringPreferencesKey("access_token_cipher")
private val KEY_REFRESH_TOKEN_CIPHER = stringPreferencesKey("refresh_token_cipher")
private val KEY_ACCESS_TOKEN_EXPIRY_EPOCH_MILLISECONDS = longPreferencesKey("access_token_expiry_epoch_milliseconds")
private const val PREFS_FILE_NAME = "de.mistatee.erglog.data.auth.session"

internal val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_FILE_NAME)

class EncryptedSessionStore(context: Context) : SessionStore {
    private val dataStore: DataStore<Preferences> = context.sessionDataStore
    private val aead: Aead = buildAead(context)

    override suspend fun getSession(): Session? {
        val stored = readStoredSession(dataStore.data.first()) ?: return null
        return try {
            Session(
                accessToken = decrypt(stored.accessTokenCipher),
                refreshToken = decrypt(stored.refreshTokenCipher),
                accessTokenExpiry = Instant.fromEpochMilliseconds(stored.expiryEpochMilliseconds),
            )
        } catch (_: Exception) {
            // Corrupt or undecryptable session data (e.g. Keystore key was invalidated, or the
            // data was written by an older, incompatible encryption scheme); treat as no
            // session, and clear the stale entry so we don't keep failing to decrypt it.
            clearSession()
            null
        }
    }

    private data class StoredSession(
        val accessTokenCipher: String,
        val refreshTokenCipher: String,
        val expiryEpochMilliseconds: Long,
    )

    private fun readStoredSession(preferences: Preferences): StoredSession? {
        val accessTokenCipher = preferences[KEY_ACCESS_TOKEN_CIPHER]
        val refreshTokenCipher = preferences[KEY_REFRESH_TOKEN_CIPHER]
        val expiryEpochMilliseconds = preferences[KEY_ACCESS_TOKEN_EXPIRY_EPOCH_MILLISECONDS]

        return accessTokenCipher?.let { accessCipher ->
            refreshTokenCipher?.let { refreshCipher ->
                expiryEpochMilliseconds?.let { expiry ->
                    StoredSession(
                        accessTokenCipher = accessCipher,
                        refreshTokenCipher = refreshCipher,
                        expiryEpochMilliseconds = expiry,
                    )
                }
            }
        }
    }

    override suspend fun saveSession(session: Session) {
        val accessTokenCipher = encrypt(session.accessToken)
        val refreshTokenCipher = encrypt(session.refreshToken)
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN_CIPHER] = accessTokenCipher
            preferences[KEY_REFRESH_TOKEN_CIPHER] = refreshTokenCipher
            preferences[KEY_ACCESS_TOKEN_EXPIRY_EPOCH_MILLISECONDS] = session.accessTokenExpiry.toEpochMilliseconds()
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    private fun encrypt(plaintext: String): String {
        val ciphertext = aead.encrypt(plaintext.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(ciphertextBase64: String): String {
        val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
        return String(aead.decrypt(ciphertext, null), Charsets.UTF_8)
    }

    private companion object {
        @Volatile
        private var tinkRegistered = false

        @Synchronized
        private fun ensureTinkRegistered() {
            if (!tinkRegistered) {
                AeadConfig.register()
                tinkRegistered = true
            }
        }

        fun buildAead(context: Context): Aead {
            ensureTinkRegistered()
            val keysetHandle =
                AndroidKeysetManager
                    .Builder()
                    .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_FILE_NAME)
                    .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
                    .withMasterKeyUri(MASTER_KEY_URI)
                    .build()
                    .keysetHandle
            @Suppress("DEPRECATION")
            return keysetHandle.getPrimitive(Aead::class.java)
        }
    }
}
