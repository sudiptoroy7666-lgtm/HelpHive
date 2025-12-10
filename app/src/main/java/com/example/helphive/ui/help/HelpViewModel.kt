// File: com/example/helphive/ui/help/HelpViewModel.kt
package com.example.helphive.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.HelpRequest
import com.example.helphive.data.firebase.RealtimeDbService
import com.example.helphive.domain.usecase.AddHelpRequestUseCase
import com.example.helphive.domain.usecase.GetHelpRequestsUseCase
import com.example.helphive.domain.usecase.UpdateHelpRequestUseCase
import com.example.helphive.domain.usecase.DeleteHelpRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val addHelpRequestUseCase: AddHelpRequestUseCase,
    private val getHelpRequestsUseCase: GetHelpRequestsUseCase,
    private val updateHelpRequestUseCase: UpdateHelpRequestUseCase,
    private val deleteHelpRequestUseCase: DeleteHelpRequestUseCase,
    private val realtimeDbService: RealtimeDbService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState

    init {
        loadHelpRequests()
    }

    fun loadHelpRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            android.util.Log.d("HelpViewModel", "About to call getHelpRequestsUseCase") // Debug log

            getHelpRequestsUseCase().onSuccess { requests ->
                android.util.Log.d(
                    "HelpViewModel",
                    "Successfully loaded ${requests.size} requests from use case"
                ) // Debug log
                _uiState.value = _uiState.value.copy(
                    helpRequests = requests,
                    isLoading = false,
                    error = null
                )
            }.onFailure { exception ->
                android.util.Log.e(
                    "HelpViewModel",
                    "Error in loadHelpRequests: ${exception.message}",
                    exception
                ) // Error log
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load help requests: ${exception.message}" // Ensure error message is set
                )
            }
        }
    }

    fun addHelpRequest(title: String, description: String, userId: String, imagePath: String) {
        viewModelScope.launch {
            val helpRequest = HelpRequest(
                requestId = java.util.UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                description = description,
                timestamp = DateUtils.getCurrentTimestamp(),
                imagePath = imagePath
            )

            addHelpRequestUseCase(helpRequest).onSuccess {
                // Update UI immediately
                val currentRequests = _uiState.value.helpRequests
                _uiState.value = _uiState.value.copy(
                    helpRequests = currentRequests + helpRequest,
                    updateSuccess = true,
                    error = null
                )

                // Refresh from server to ensure consistency
                loadHelpRequests()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add help request: ${exception.message}"
                )
            }
        }
    }

    fun updateHelpRequest(helpRequest: HelpRequest, imageBase64: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Upload image if provided
            if (imageBase64.isNotEmpty() && helpRequest.imagePath.isNotEmpty()) {
                realtimeDbService.uploadImage(helpRequest.imagePath, imageBase64)
            }

            updateHelpRequestUseCase(helpRequest).onSuccess {
                // Update UI immediately
                val currentRequests = _uiState.value.helpRequests
                val updatedRequests = currentRequests.map {
                    if (it.requestId == helpRequest.requestId) helpRequest else it
                }
                _uiState.value = _uiState.value.copy(
                    helpRequests = updatedRequests,
                    isLoading = false,
                    updateSuccess = true,
                    error = null
                )

                // Refresh from server to ensure consistency
                loadHelpRequests()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update help request: ${exception.message}"
                )
            }
        }
    }

    fun deleteHelpRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            deleteHelpRequestUseCase(requestId).onSuccess {
                // Update UI immediately
                val currentRequests = _uiState.value.helpRequests
                val updatedRequests = currentRequests.filter { it.requestId != requestId }
                _uiState.value = _uiState.value.copy(
                    helpRequests = updatedRequests,
                    isLoading = false,
                    error = null
                )

                // Refresh from server to ensure consistency
                loadHelpRequests()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete help request: ${exception.message}"
                )
            }
        }
    }
}