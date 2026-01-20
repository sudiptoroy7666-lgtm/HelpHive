package com.example.helphive.domain.repository

interface AiRepository {
    suspend fun getSupportiveMessage(mood: String, note: String): Result<String>
}
