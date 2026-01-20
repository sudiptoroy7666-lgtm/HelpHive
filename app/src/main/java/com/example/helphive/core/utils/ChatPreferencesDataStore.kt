// File: com/example/helphive/core/utils/ChatPreferencesDataStore.kt
package com.example.helphive.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.helphive.data.firebase.ChatConversation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create the DataStore instance
private val Context.chatPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_preferences")

@Singleton
class ChatPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson // Inject Gson for serialization
) {

    private object PreferencesKeys {
        val CHAT_CONVERSATIONS = stringPreferencesKey("chat_conversations")
        val USER_CACHE_PREFIX = "user_cache_" // Prefix for caching individual user details
    }

    suspend fun saveChatConversations(conversations: List<ChatConversation>) {
        val jsonString = gson.toJson(conversations)
        context.chatPreferencesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CHAT_CONVERSATIONS] = jsonString
        }
    }

    suspend fun getChatConversations(): List<ChatConversation> {
        val preferences = context.chatPreferencesDataStore.data.firstOrNull()
        val jsonString = preferences?.get(PreferencesKeys.CHAT_CONVERSATIONS) ?: return emptyList()
        val type = object : TypeToken<List<ChatConversation>>() {}.type
        return try {
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            // Log error if needed
            emptyList()
        }
    }

    suspend fun cacheUserDetails(userId: String, name: String, profileImagePath: String) {
        val userKey = stringPreferencesKey("${PreferencesKeys.USER_CACHE_PREFIX}$userId")
        context.chatPreferencesDataStore.edit { preferences ->
            preferences[userKey] = "$name|$profileImagePath" // Simple delimiter, consider JSON for complex data
        }
    }

    suspend fun getCachedUserDetails(userId: String): Pair<String, String>? { // name, profileImagePath
        val userKey = stringPreferencesKey("${PreferencesKeys.USER_CACHE_PREFIX}$userId")
        val preferences = context.chatPreferencesDataStore.data.firstOrNull()
        val cachedString = preferences?.get(userKey) ?: return null
        val parts = cachedString.split("|", limit = 2)
        return if (parts.size == 2) Pair(parts[0], parts[1]) else null
    }

    suspend fun clearCachedConversations() {
        context.chatPreferencesDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CHAT_CONVERSATIONS)
        }
    }
}