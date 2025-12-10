// File: com/example/helphive/auth/ForgotPasswordViewModel.kt
package com.example.helphive.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.domain.usecase.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel // This is crucial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // This annotation is required
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            sendPasswordResetEmailUseCase(email).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEmailSent = true,
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
}

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val error: String? = null
)