package com.example.helphive.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.PreferencesManager
import com.example.helphive.domain.usecase.SignInUseCase
import com.example.helphive.domain.usecase.SignUpUseCase
import com.example.helphive.domain.usecase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Add this data class to the same file
// In AuthViewModel.kt
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val needsProfile: Boolean = false,
    val showLoginTab: Boolean = false, // Add this new state
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signIn(email: String, password: String, rememberMe: Boolean = false, preferencesManager: PreferencesManager? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            signInUseCase(email, password).onSuccess { userId ->
                // Check if user has completed their profile by checking if all required fields exist
                getUserUseCase(userId).onSuccess { user ->
                    // Check if user has completed profile (name, address, gender)
                    if (user.name.isNotEmpty() && user.address.isNotEmpty() && user.gender.isNotEmpty()) {
                        // Profile exists and is complete, save credentials and navigate to main
                        preferencesManager?.saveLoginCredentials(email, "", rememberMe, userId) // Don't save password

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            needsProfile = false, // Profile already exists and is complete
                            error = null
                        )
                    } else {
                        // Profile exists but is incomplete, redirect to profile completion
                        preferencesManager?.saveLoginCredentials(email, "", rememberMe, userId) // Don't save password

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            needsProfile = true, // Redirect to profile completion
                            error = null
                        )
                    }
                }.onFailure {
                    // User doesn't exist in Firestore, redirect to profile creation
                    // This should not happen after successful sign-in, but handle it
                    preferencesManager?.saveLoginCredentials(email, "", rememberMe, userId) // Don't save password

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        needsProfile = true, // Redirect to profile creation
                        error = null
                    )
                }
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        }
    }

    // In AuthViewModel.kt
    // In AuthViewModel.kt - Updated signUp function
    fun signUp(email: String, password: String, name: String, rememberMe: Boolean = false, preferencesManager: PreferencesManager? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            signUpUseCase(email, password, name).onSuccess { userId ->
                // After successful signup, navigate back to login tab
                preferencesManager?.saveLoginCredentials(email, "", rememberMe, userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false, // Don't log in automatically
                    needsProfile = false, // Don't navigate to profile
                    showLoginTab = true, // Show login tab after signup
                    error = null
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        }
    }

    fun checkProfileCompletion(userId: String) {
        viewModelScope.launch {
            getUserUseCase(userId).onSuccess { user ->
                if (user.name.isNotEmpty() && user.address.isNotEmpty() && user.gender.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        needsProfile = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        needsProfile = true,
                        error = null
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    needsProfile = true,
                    error = null
                )
            }
        }
    }
}