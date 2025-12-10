package com.example.helphive.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphive.core.utils.DateUtils
import com.example.helphive.data.model.ChatMessage
import com.example.helphive.domain.usecase.GetChatMessagesUseCase
import com.example.helphive.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Add this data class to the same file
data class MessagesUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase
) : ViewModel() {

    private val _messagesState = MutableStateFlow(MessagesUiState())
    val messagesState: StateFlow<MessagesUiState> = _messagesState.asStateFlow()

    // Cache for specific conversation
    private var cachedMessages: MutableMap<String, List<ChatMessage>> = mutableMapOf()

    fun startListeningForMessages(requestId: String) {
        // Load cached messages immediately
        cachedMessages[requestId]?.let { cached ->
            _messagesState.value = _messagesState.value.copy(
                messages = cached,
                isLoading = false,
                error = null
            )
        }

        viewModelScope.launch {
            // Use the observe method for real-time updates
            getChatMessagesUseCase.observe(requestId).collect { result ->
                result.onSuccess { messages ->
                    // Cache the messages
                    cachedMessages[requestId] = messages
                    _messagesState.value = _messagesState.value.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                }.onFailure { exception ->
                    _messagesState.value = _messagesState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            }
        }
    }

    fun sendMessage(requestId: String, message: String, senderId: String) {
        viewModelScope.launch {
            val chatMessage = ChatMessage(
                messageId = java.util.UUID.randomUUID().toString(),
                requestId = requestId,
                senderId = senderId,
                message = message,
                timestamp = DateUtils.getCurrentTimestamp()
            )

            // Add to local cache immediately for better UX
            val currentMessages = cachedMessages[requestId] ?: emptyList()
            cachedMessages[requestId] = currentMessages + chatMessage
            _messagesState.value = _messagesState.value.copy(
                messages = cachedMessages[requestId] ?: emptyList(),
                error = null
            )

            sendChatMessageUseCase(chatMessage).onSuccess {
                // The real-time listener will automatically update messages
                // No need to manually refresh
            }.onFailure { exception ->
                // Remove from cache if failed
                cachedMessages[requestId] = cachedMessages[requestId]?.filter { it.messageId != chatMessage.messageId } ?: emptyList()
                _messagesState.value = _messagesState.value.copy(
                    error = exception.message
                )
            }
        }
    }
}