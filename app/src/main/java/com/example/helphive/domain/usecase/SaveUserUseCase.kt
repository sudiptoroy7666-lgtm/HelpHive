package com.example.helphive.domain.usecase

import com.example.helphive.data.model.User
import com.example.helphive.domain.repository.UserRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<String> {
        return userRepository.saveUser(user)
    }
}