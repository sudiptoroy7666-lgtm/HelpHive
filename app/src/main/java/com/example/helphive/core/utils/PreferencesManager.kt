// Updated PreferencesManager.kt - Add this to your build.gradle first:
// implementation 'androidx.security:security-crypto:1.1.0-alpha06'

package com.example.helphive.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveLoginCredentials(email: String, password: String, rememberMe: Boolean, userId: String = "") {
        with(prefs.edit()) {
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            if (rememberMe) {
                putString(KEY_EMAIL, email)
                putString(KEY_USER_ID, userId)
            } else {
                remove(KEY_EMAIL)
                remove(KEY_USER_ID)
            }
            apply()
        }
    }

    fun getSavedEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun getSavedPassword(): String {
        return prefs.getString(KEY_USER_ID, "") ?: "" // We're not storing password anymore, but returning empty
    }

    fun getSavedUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }

    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearSavedCredentials() {
        with(prefs.edit()) {
            remove(KEY_EMAIL)
            remove(KEY_USER_ID)
            remove(KEY_REMEMBER_ME)
            apply()
        }
    }
}