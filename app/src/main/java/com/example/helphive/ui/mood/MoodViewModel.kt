package com.example.helphive.ui.mood


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.Mood
import com.example.helphive.domain.usecase.AddMoodUseCase
import com.example.helphive.domain.usecase.GetUserMoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val addMoodUseCase: AddMoodUseCase,
    private val getUserMoodsUseCase: GetUserMoodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState

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
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message
                )
            }
        }
    }
}

data class MoodUiState(
    val moods: List<Mood> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)