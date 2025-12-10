package com.example.helphive.ui.kindness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.Kindness
import com.example.helphive.domain.usecase.AddKindnessUseCase
import com.example.helphive.domain.usecase.GetKindnessFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KindnessViewModel @Inject constructor(
    private val addKindnessUseCase: AddKindnessUseCase,
    private val getKindnessFeedUseCase: GetKindnessFeedUseCase


) : ViewModel() {

    private val _uiState = MutableStateFlow(KindnessUiState())
    val uiState: StateFlow<KindnessUiState> = _uiState

    // Cache for faster loading
    private var cachedKindnessFeed: List<Kindness>? = null

    init {
        loadKindnessFeed()
    }

    fun loadKindnessFeed() {
        // Show cached data immediately if available
        cachedKindnessFeed?.let { cached ->
            _uiState.value = _uiState.value.copy(
                kindnessFeed = cached,
                isLoading = false,
                error = null
            )
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getKindnessFeedUseCase().onSuccess { kindnessList ->
                // Update cache
                cachedKindnessFeed = kindnessList
                _uiState.value = _uiState.value.copy(
                    kindnessFeed = kindnessList,
                    isLoading = false,
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

    fun addKindness(text: String, userId: String) {
        viewModelScope.launch {
            val kindness = Kindness(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                text = text,
                timestamp = DateUtils.getCurrentTimestamp()
            )

            // Add to local cache immediately for better UX
            cachedKindnessFeed = (cachedKindnessFeed ?: emptyList()) + kindness
            _uiState.value = _uiState.value.copy(
                kindnessFeed = cachedKindnessFeed ?: emptyList(),
                isLoading = false,
                error = null
            )

            addKindnessUseCase(kindness).onSuccess {
                // Refresh from server to ensure consistency
                loadKindnessFeed()
            }.onFailure { exception ->
                // Remove from cache if failed
                cachedKindnessFeed = cachedKindnessFeed?.filter { it.id != kindness.id }
                _uiState.value = _uiState.value.copy(
                    error = exception.message
                )
            }
        }
    }
}

data class KindnessUiState(
    val kindnessFeed: List<Kindness> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)