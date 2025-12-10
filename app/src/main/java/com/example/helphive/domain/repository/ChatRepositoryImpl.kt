package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.ChatConversation
import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.model.ChatMessage
import com.example.helphive.domain.repository.ChatRepository
import javax.inject.Inject

// In ChatRepositoryImpl.kt - add this method
class ChatRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : ChatRepository {

    override suspend fun addChatMessage(message: ChatMessage): Result<String> {
        return firestoreService.addChatMessage(message)
    }

    override suspend fun getChatMessages(requestId: String): Result<List<ChatMessage>> {
        return firestoreService.getChatMessages(requestId)
    }

    override suspend fun getChatConversations(userId: String): Result<List<ChatConversation>> {
        return firestoreService.getChatConversations(userId)
    }

    // Add this method
    override fun observeChatMessages(requestId: String): kotlinx.coroutines.flow.Flow<Result<List<ChatMessage>>> {
        return firestoreService.observeChatMessages(requestId)
    }
}