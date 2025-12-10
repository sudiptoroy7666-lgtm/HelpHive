package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.ChatRepository
import com.example.helphive.data.model.ChatMessage
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    // Regular method for one-time fetch
    suspend operator fun invoke(requestId: String): Result<List<ChatMessage>> {
        return chatRepository.getChatMessages(requestId)
    }

    // Separate method for real-time updates (not using invoke)
    fun observe(requestId: String): Flow<Result<List<ChatMessage>>> {
        return chatRepository.observeChatMessages(requestId)
    }
}