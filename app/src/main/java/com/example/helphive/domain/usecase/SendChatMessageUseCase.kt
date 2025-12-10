package com.example.helphive.domain.usecase


import com.example.helphive.data.model.ChatMessage
import com.example.helphive.domain.repository.ChatRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: ChatMessage): Result<String> {
        return chatRepository.addChatMessage(message)
    }
}