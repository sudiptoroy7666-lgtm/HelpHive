package com.example.helphive.ui.mood


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.Mood
import com.example.helphive.domain.usecase.AddMoodUseCase
import com.example.helphive.domain.usecase.GetUserMoodsUseCase
import com.example.helphive.domain.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val addMoodUseCase: AddMoodUseCase,
    private val getUserMoodsUseCase: GetUserMoodsUseCase,
    private val aiRepository: com.example.helphive.domain.repository.AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState

    private val _supportiveMessage = MutableStateFlow<String?>(null)
    val supportiveMessage: StateFlow<String?> = _supportiveMessage

    private val negativeMoods = setOf("😢", "😠", "😞", "😔", "😫", "😩", "😤", "😡", "😰", "😟")

    fun loadUserMoods(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getUserMoodsUseCase(userId).onSuccess { moods ->
                _uiState.value = _uiState.value.copy(
                    moods = moods,
                    isLoading = false
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
        }
    }

    fun addMood(emoji: String, note: String, userId: String) {
        viewModelScope.launch {
            val mood = Mood(
                moodId = java.util.UUID.randomUUID().toString(),
                userId = userId,
                emoji = emoji,
                note = note,
                timestamp = DateUtils.getCurrentTimestamp()
            )

            addMoodUseCase(mood).onSuccess {
                // Refresh the list
                loadUserMoods(userId)
                
                // Fetch AI supportive message for any mood
                fetchSupportiveMessage(emoji, note)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message
                )
            }
        }
    }

    private fun fetchSupportiveMessage(emoji: String, note: String) {
        viewModelScope.launch {
            aiRepository.getSupportiveMessage(emoji, note).onSuccess { message ->
                _supportiveMessage.value = message
            }.onFailure { error ->
                // Fallback with error info to help debug
                Log.e("MoodViewModel", "AI Fetch Failed", error)
                _supportiveMessage.value = "Stay strong! You've got this.\n(Note: AI service is currently unavailable: ${error.localizedMessage})"
            }
        }
    }

    fun clearSupportiveMessage() {
        _supportiveMessage.value = null
    }
}

data class MoodUiState(
    val moods: List<Mood> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)