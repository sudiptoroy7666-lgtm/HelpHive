package com.example.helphive.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val profileImagePath: String = "",
    val address: String = "",
    val gender: String = "",
    val profileImageBase64: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "profileImagePath" to profileImagePath,
            "address" to address,
            "gender" to gender
        )
    }
}