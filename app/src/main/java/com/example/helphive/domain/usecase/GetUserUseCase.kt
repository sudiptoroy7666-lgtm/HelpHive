// GetUserUseCase
package com.example.helphive.domain.usecase

import com.example.helphive.data.model.User
import com.example.helphive.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.getUser(userId)
    }
}