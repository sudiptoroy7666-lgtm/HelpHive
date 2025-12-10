package com.example.helphive.domain.repository

import com.example.helphive.data.model.User

interface UserRepository {
    suspend fun saveUser(user: User): Result<String>
    suspend fun getUser(userId: String): Result<User>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<String>
}