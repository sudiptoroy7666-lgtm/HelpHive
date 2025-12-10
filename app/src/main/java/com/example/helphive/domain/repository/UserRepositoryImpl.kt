// Fixed UserRepositoryImpl.kt
package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.model.User
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : UserRepository {

    override suspend fun saveUser(user: User): Result<String> {
        return firestoreService.saveUser(user)
    }

    override suspend fun getUser(userId: String): Result<User> {
        return firestoreService.getUser(userId)
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        return firestoreService.getAllUsers()
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<String> {
        return firestoreService.updateUser(userId, updates) // Fixed: Actually call the service method
    }
}