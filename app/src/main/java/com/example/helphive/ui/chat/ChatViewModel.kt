// File: com/example/helphive/ui/chat/ChatViewModel.kt
package com.example.helphive.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.data.firebase.ChatConversation
import com.example.helphive.domain.usecase.GetChatConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatConversationsUseCase: GetChatConversationsUseCase
) : ViewModel() {

    private val _conversationsState = MutableStateFlow(ConversationsUiState())
    val conversationsState: StateFlow<ConversationsUiState> = _conversationsState

    fun loadChatConversations(userId: String) {
        viewModelScope.launch {
            _conversationsState.value = _conversationsState.value.copy(isLoading = true)

            getChatConversationsUseCase(userId)
                .collect { conversations ->
                    _conversationsState.value = _conversationsState.value.copy(
                        conversations = conversations,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}

data class ConversationsUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)