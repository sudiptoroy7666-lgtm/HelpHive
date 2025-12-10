package com.example.helphive.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.data.model.User
import com.example.helphive.data.firebase.RealtimeDbService
import com.example.helphive.domain.usecase.GetUserUseCase
import com.example.helphive.domain.usecase.UpdateUserUseCase
import com.example.helphive.domain.usecase.SaveUserUseCase
import com.example.helphive.data.firebase.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val realtimeDbService: RealtimeDbService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadUser(userId: String) {
        if (_uiState.value.isLoading) return // Prevent duplicate loads

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getUserUseCase(userId).onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false,
                    error = null
                )

                // Load profile image if exists
                if (user.profileImagePath.isNotEmpty()) {
                    loadProfileImage(user.profileImagePath)
                }
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load user profile: ${exception.message}"
                )
            }
        }
    }

    fun createOrUpdateProfile(userId: String, name: String, address: String, gender: String, imageBase64: String, isProfileCreation: Boolean = false) {
        if (_uiState.value.isLoading) return // Prevent duplicate submissions

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val imagePath = if (imageBase64.isNotEmpty()) "profile/$userId" else ""

            // Update profile image in Realtime DB if provided
            if (imageBase64.isNotEmpty()) {
                realtimeDbService.uploadImage(imagePath, imageBase64)
            }

            val updates = mutableMapOf<String, Any>().apply {
                put("name", name)
                put("address", address)
                put("gender", gender)
                if (imagePath.isNotEmpty()) {
                    put("profileImagePath", imagePath)
                }
            }

            updateUserUseCase(userId, updates).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    updateSuccess = true,
                    error = null
                )
                // Reload user data
                loadUser(userId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update profile: ${exception.message}"
                )
            }
        }
    }

    fun loadProfileImage(imagePath: String) {
        // Cancel previous image loading if any
        viewModelScope.launch {
            realtimeDbService.observeImage(imagePath) { base64String ->
                if (base64String != null) {
                    val bitmap = com.example.helphive.core.utils.Base64Utils.base64ToBitmap(base64String)
                    if (bitmap != null) {
                        _uiState.value = _uiState.value.copy(
                            profileImageBitmap = bitmap
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load profile image"
                        )
                    }
                }
            }
        }
    }

    // Add method to clear image bitmap when not needed to prevent memory leaks
    fun clearImageBitmap() {
        _uiState.value = _uiState.value.copy(profileImageBitmap = null)
    }
}