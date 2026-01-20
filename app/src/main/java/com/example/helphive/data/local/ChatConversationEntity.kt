package com.example.helphive.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey
    val otherUserId: String, // Primary key as one conversation per user
    val requestId: String,
    val otherUserName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val userProfileImage: String
)
