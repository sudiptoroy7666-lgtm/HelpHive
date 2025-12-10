package com.example.helphive.ui.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.data.model.Mood
import com.example.helphive.domain.usecase.GetAllMoodsUseCase  // We need to create this
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodStatisticsViewModel @Inject constructor(
    private val getAllMoodsUseCase: GetAllMoodsUseCase  // This is new
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodStatisticsUiState())
    val uiState: StateFlow<MoodStatisticsUiState> = _uiState

    fun loadAllUsersMoods() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAllMoodsUseCase().onSuccess { moods ->
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
}

data class MoodStatisticsUiState(
    val moods: List<Mood> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)