package com.example.helphive.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.helphive.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreService: FirestoreService
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!

            if (!user.isEmailVerified) {
                return Result.failure(Exception("Please verify your email address"))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            user.sendEmailVerification().await()

            user.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()).await()

            val userProfile = User(
                userId = user.uid,
                name = name,
                email = email,
                address = "",
                gender = "",
                profileImagePath = ""
            )
            firestoreService.saveUser(userProfile)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reauthenticateWithPassword(password: String): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: return Result.failure(Exception("User not authenticated"))
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val user = getCurrentUser() ?: return Result.failure(Exception("User not authenticated"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Password reset email sent successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}