package com.example.helphive.data.model

data class ChatMessage(
    val messageId: String = "",
    val requestId: String = "",
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val senderName: String = "", // For display
    val senderProfileImage: String = "" // For display
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "messageId" to messageId,
            "requestId" to requestId,
            "senderId" to senderId,
            "message" to message,
            "timestamp" to timestamp
        )
    }
}