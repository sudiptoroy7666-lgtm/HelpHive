// File: com/example/helphive/core/utils/UserPreferencesDataStore.kt
package com.example.helphive.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create the DataStore instance
private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Make context available to HelpRepositoryImpl if needed
    // This is a simple way, you could also inject Context separately into HelpRepositoryImpl if preferred
    val contextForDataStore = context

    // You can define keys here if you plan to cache other user-specific data later
    // For now, HelpRepositoryImpl will define its own key for help requests
}
