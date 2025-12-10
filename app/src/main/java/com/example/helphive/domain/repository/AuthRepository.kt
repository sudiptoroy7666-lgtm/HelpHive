package com.example.helphive.domain.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String, name: String): Result<String>
    suspend fun reauthenticateWithPassword(password: String): Result<Unit>  // Add this line
    suspend fun deleteCurrentUser(): Result<Unit>  // Add this line
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<String> // Add this
}