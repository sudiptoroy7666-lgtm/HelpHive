// File: com/example/helphive/core/utils/HelpPreferencesDataStore.kt
package com.example.helphive.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.helphive.data.model.HelpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create the DataStore instance for help requests
private val Context.helpPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "help_preferences")

@Singleton
class HelpPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson // Inject Gson
) {

    private object PreferencesKeys {
        val CACHED_HELP_REQUESTS = stringPreferencesKey("cached_help_requests")
    }

    suspend fun saveCachedHelpRequests(requests: List<HelpRequest>) {
        val jsonString = gson.toJson(requests)
        context.helpPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_HELP_REQUESTS] = jsonString
        }
    }

    suspend fun getCachedHelpRequests(): List<HelpRequest> {
        val preferences = context.helpPreferencesDataStore.data.firstOrNull()
        val jsonString = preferences?.get(PreferencesKeys.CACHED_HELP_REQUESTS) ?: return emptyList()
        val type = object : TypeToken<List<HelpRequest>>() {}.type
        return try {
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("HelpPrefsDataStore", "Error parsing cached help requests", e)
            emptyList()
        }
    }

    suspend fun clearCachedHelpRequests() {
        context.helpPreferencesDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CACHED_HELP_REQUESTS)
        }
    }
}