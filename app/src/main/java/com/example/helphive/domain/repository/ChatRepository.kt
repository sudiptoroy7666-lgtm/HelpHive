package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.ChatConversation
import com.example.helphive.data.model.ChatMessage
import com.example.helphive.data.model.HelpRequest

// In ChatRepository.kt - add this import and method
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun addChatMessage(message: ChatMessage): Result<String>
    suspend fun getChatMessages(requestId: String): Result<List<ChatMessage>>
    suspend fun getChatConversations(userId: String): Result<List<ChatConversation>>
    // Add this method
    fun observeChatMessages(requestId: String): kotlinx.coroutines.flow.Flow<Result<List<ChatMessage>>>
}