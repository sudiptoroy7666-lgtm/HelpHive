package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, updates: Map<String, Any>): Result<String> {
        return userRepository.updateUser(userId, updates)
    }
}