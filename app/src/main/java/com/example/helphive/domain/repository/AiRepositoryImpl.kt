package com.example.helphive.domain.repository

import com.example.helphive.BuildConfig
import com.example.helphive.data.remote.Content
import com.example.helphive.data.remote.GeminiRequest
import com.example.helphive.data.remote.GeminiService
import com.example.helphive.data.remote.Part
import android.util.Log
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService
) : AiRepository {

    override suspend fun getSupportiveMessage(mood: String, note: String): Result<String> {
        return try {
            val prompt = "The user is feeling $mood and wrote this note: \"$note\". " +
                    "Please provide a supportive message to cheer them up. " +
                    "The tone should be tailored to the mood: " +
                    "If they are sad (😢, 😞, 😔), be extra gentle and empathetic. " +
                    "If they are angry (😠, 😡, 😤), be calm and grounding. " +
                    "If they are anxious (😰, 😟, 😫), be reassuring and peaceful. " +
                    "If they are neutral or otherwise, be encouraging and positive. " +
                    "Keep the response under 100 words."
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )

            Log.d("AiRepository", "Requesting AI response for mood: $mood")
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty()) {
                Log.e("AiRepository", "Gemini API Key is empty!")
                return Result.failure(Exception("API Key is missing"))
            }

            val response = geminiService.generateContent(apiKey, request)
            
            val message = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (message != null) {
                Log.d("AiRepository", "AI success: $message")
                Result.success(message)
            } else {
                Log.e("AiRepository", "AI response candidates list is empty or message is null")
                try {
                    val modelsList = geminiService.listModels(apiKey)
                    Log.d("AiRepository", "Available Models: ${modelsList.models.map { it.name }}")
                } catch (le: Exception) {
                    Log.e("AiRepository", "Double Failure: Also failed to list models: ${le.message}")
                }
                Result.failure(Exception("No response from AI"))
            }
        } catch (e: Exception) {
            Log.e("AiRepository", "AI Error: ${e.message}", e)
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val modelsList = geminiService.listModels(apiKey)
                Log.d("AiRepository", "Available Models: ${modelsList.models.map { it.name }}")
            } catch (le: Exception) {
                Log.e("AiRepository", "Double Failure: Also failed to list models: ${le.message}")
            }
            Result.failure(e)
        }
    }
}
