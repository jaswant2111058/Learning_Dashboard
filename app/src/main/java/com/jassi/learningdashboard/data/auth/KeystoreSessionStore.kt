package com.jassi.learningdashboard.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreSessionStore @Inject constructor(
    context: Context,
    private val crypto: KeystoreCrypto,
) : SessionStore {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun save(response: LoginResponse) {
        val encryptedToken = runCatching { crypto.encrypt(response.accessToken) }
            .onFailure { Log.e(TAG, "Could not encrypt the session token", it) }
            .getOrNull() ?: return

        preferences.edit()
            .putString(KEY_TOKEN, encryptedToken)
            .putString(KEY_EMAIL, response.email)
            .putString(KEY_NAME, response.displayName)
            .putLong(KEY_EXPIRES_AT, response.expiresAtMillis)
            .apply()
    }

    override fun read(): Session? {
        val expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0L)

        if (expiresAt <= System.currentTimeMillis()) {
            if (preferences.contains(KEY_TOKEN)) clear()
            return null
        }

        val stored = preferences.getString(KEY_TOKEN, null) ?: return null
        val token = crypto.decrypt(stored) ?: run {
            clear()
            return null
        }

        return Session(
            accessToken = token,
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            displayName = preferences.getString(KEY_NAME, "").orEmpty(),
            expiresAtMillis = expiresAt,
        )
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val TAG = "SessionStore"
        const val FILE_NAME = "session_store"
        const val KEY_TOKEN = "access_token"
        const val KEY_EMAIL = "email"
        const val KEY_NAME = "display_name"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
