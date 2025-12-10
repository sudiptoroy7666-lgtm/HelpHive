package com.example.helphive.data.model

data class Mood(
    val moodId: String = "",
    val userId: String = "",
    val emoji: String = "",
    val note: String = "",
    val timestamp: Long = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "moodId" to moodId,
            "userId" to userId,
            "emoji" to emoji,
            "note" to note,
            "timestamp" to timestamp
        )
    }
}