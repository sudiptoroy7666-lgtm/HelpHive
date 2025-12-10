package com.example.helphive.domain.repository

import com.example.helphive.data.model.Mood

interface MoodRepository {
    suspend fun addMood(mood: Mood): Result<String>
    suspend fun getUserMoods(userId: String): Result<List<Mood>>
    suspend fun getAllMoods(): Result<List<Mood>>  // Add this new method
}