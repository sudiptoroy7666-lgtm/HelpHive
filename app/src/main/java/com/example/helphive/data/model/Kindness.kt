// Updated Kindness data model
package com.example.helphive.data.model

data class Kindness(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val userName: String = "", // Add user name
    val userProfileImage: String = "" // Add user profile image path
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "text" to text,
            "timestamp" to timestamp
        )
    }
}