package com.example.helphive.data.repository

import com.example.helphive.data.firebase.AuthService
import com.example.helphive.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<String> {
        return authService.signIn(email, password).map { user ->
            user.uid
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<String> {
        return authService.signUp(email, password, name).map { user ->
            user.uid
        }
    }


    override suspend fun sendPasswordResetEmail(email: String): Result<String> {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun reauthenticateWithPassword(password: String): Result<Unit> {
        return authService.reauthenticateWithPassword(password)
    }

    override suspend fun deleteCurrentUser(): Result<Unit> {
        return authService.deleteCurrentUser()
    }

    override fun getCurrentUserId(): String? {
        return authService.getCurrentUser()?.uid
    }

    override fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    override fun signOut() {
        authService.signOut()
    }
}