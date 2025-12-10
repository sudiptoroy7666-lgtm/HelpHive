package com.example.helphive.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("offline_cache", Context.MODE_PRIVATE)

    // Save any object (non-suspend, cheap)
    fun <T> saveDataSync(key: String, data: T) {
        val json = gson.toJson(data)
        prefs.edit().putString(key, json).apply()
    }

    // Suspend-safe: save large payloads off main thread
    suspend fun <T> saveData(key: String, data: T) = withContext(Dispatchers.IO) {
        val json = gson.toJson(data)
        prefs.edit().putString(key, json).apply()
    }

    // Inline + reified for type-safe sync read
    internal inline fun <reified T> getDataSync(key: String, default: T? = null): T? {
        val json = prefs.getString(key, null) ?: return default
        return try {
            gson.fromJson(json, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            default
        }
    }

    // Suspend version for large payloads
    suspend fun <T> getData(key: String, default: T? = null): T? = withContext(Dispatchers.IO) {
        // Call sync inline function safely
        getDataSync(key, default)
    }

    // Add item to a list stored under key
    suspend fun <T> addToList(key: String, newItem: T) {
        val currentList: List<T> = getData(key, emptyList()) ?: emptyList()
        val updatedList = currentList.toMutableList().also { it.add(newItem) }
        saveData(key, updatedList)
    }

    // Update item in a list by matching predicate
    suspend fun <T> updateInList(
        key: String,
        match: (T) -> Boolean,
        updatedItem: T
    ) {
        val currentList: List<T> = getData(key, emptyList()) ?: emptyList()
        val updatedList = currentList.map { if (match(it)) updatedItem else it }
        saveData(key, updatedList)
    }

    // Cache user metadata
    suspend fun cacheUser(userId: String, userName: String, userProfileImagePathOrUrl: String) {
        val userKey = "user_$userId"
        val userData = mapOf(
            "userId" to userId,
            "name" to userName,
            "profileImagePath" to userProfileImagePathOrUrl
        )
        saveData(userKey, userData)
    }

    // Get cached user
    suspend fun getCachedUser(userId: String): Pair<String, String>? {
        val userKey = "user_$userId"
        val userData: Map<String, String>? = getData(userKey)
        return userData?.let { Pair(it["name"] ?: "Unknown User", it["profileImagePath"] ?: "") }
    }

    // Clear key synchronously
    fun clearDataSync(key: String) {
        prefs.edit().remove(key).apply()
    }

    // Clear key asynchronously
    suspend fun clearData(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }
}
