package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<String> {
        return authRepository.signUp(email, password, name)
    }
}