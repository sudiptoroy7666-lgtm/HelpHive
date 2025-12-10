package com.example.helphive.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HelpRequest(
    val requestId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0,
    val imagePath: String = "", // Realtime DB path
    val imageBase64: String = "", // For display
    val userName: String = "", // User's name for display
    val userProfileImage: String = "" // User's profile image for display
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "requestId" to requestId,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "timestamp" to timestamp,
            "imagePath" to imagePath
        )
    }
}