// Example for GetChatConversationsUseCase
package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatConversationsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(userId: String) = chatRepository.getChatConversations(userId)
}