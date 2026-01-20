package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.ChatConversation
import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.local.ChatConversationEntity
import com.example.helphive.data.local.ChatDao
import com.example.helphive.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val chatDao: ChatDao
) : ChatRepository {

    override suspend fun addChatMessage(message: ChatMessage): Result<String> {
        return firestoreService.addChatMessage(message)
    }

    override suspend fun getChatMessages(requestId: String): Result<List<ChatMessage>> {
        return firestoreService.getChatMessages(requestId)
    }

    override fun getChatConversations(userId: String): Flow<List<ChatConversation>> = channelFlow {
        // 1. Observe local database
        val localJob = launch {
            chatDao.getConversations().collect { entities ->
                val domainList = entities.map { entity ->
                    ChatConversation(
                        requestId = entity.requestId,
                        otherUserId = entity.otherUserId,
                        otherUserName = entity.otherUserName,
                        lastMessage = entity.lastMessage,
                        lastMessageTime = entity.lastMessageTime,
                        unreadCount = entity.unreadCount,
                        userProfileImage = entity.userProfileImage
                    )
                }
                send(domainList)
            }
        }

        // 2. Fetch from network and update local database
        launch(Dispatchers.IO) {
            val result = firestoreService.getChatConversations(userId)
            result.onSuccess { conversations ->
                val entities = conversations.map { conversation ->
                    ChatConversationEntity(
                        otherUserId = conversation.otherUserId,
                        requestId = conversation.requestId,
                        otherUserName = conversation.otherUserName,
                        lastMessage = conversation.lastMessage,
                        lastMessageTime = conversation.lastMessageTime,
                        unreadCount = conversation.unreadCount,
                        userProfileImage = conversation.userProfileImage
                    )
                }
                chatDao.insertConversations(entities)
            }
            // Optional: handle failure
        }
    }

    override fun observeChatMessages(requestId: String): Flow<Result<List<ChatMessage>>> {
        return firestoreService.observeChatMessages(requestId)
    }
}