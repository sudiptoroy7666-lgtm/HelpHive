package com.example.helphive.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @GET("v1beta/models")
    suspend fun listModels(
        @Query("key") apiKey: String
    ): GeminiModelListResponse
}

data class GeminiModelListResponse(
    val models: List<GeminiModel>
)

data class GeminiModel(
    val name: String,
    val baseModelId: String?,
    val version: String?,
    val displayName: String?,
    val description: String?,
    val inputTokenLimit: Int?,
    val outputTokenLimit: Int?,
    val supportedGenerationMethods: List<String>?
)

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)
