package com.example.helphive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.data.firebase.AuthService
import com.example.helphive.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val helpRepository: HelpRepository,
    private val moodRepository: MoodRepository,
    private val kindnessRepository: KindnessRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState

    fun deleteAccount(userId: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Re-authenticate user with password
                authService.reauthenticateWithPassword(password).onSuccess {
                    // Delete user data from Firestore
                    deleteUserData(userId)

                    // Delete user from Firebase Auth
                    authService.deleteCurrentUser().onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAccountDeleted = true,
                            error = null
                        )
                    }.onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to delete account: ${exception.message}"
                        )
                    }
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Authentication failed: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error deleting account: ${e.message}"
                )
            }
        }
    }

    private suspend fun deleteUserData(userId: String) {
        try {
            // Delete user profile from Firestore using FieldValue.delete()
            userRepository.updateUser(userId, mapOf(
                "name" to com.google.firebase.firestore.FieldValue.delete(),
                "email" to com.google.firebase.firestore.FieldValue.delete(),
                "address" to com.google.firebase.firestore.FieldValue.delete(),
                "gender" to com.google.firebase.firestore.FieldValue.delete(),
                "profileImagePath" to com.google.firebase.firestore.FieldValue.delete()
            ))

            // For complete data deletion, you'd need to implement specific methods in your repositories
            // to delete all user-related data (help requests, moods, kindness acts, chat messages)

        } catch (e: Exception) {
            // Log error but continue with account deletion
        }
    }
}

data class DeleteAccountUiState(
    val isLoading: Boolean = false,
    val isAccountDeleted: Boolean = false,
    val error: String? = null
)