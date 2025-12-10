// File: com/example/helphive/domain/usecase/SendPasswordResetEmailUseCase.kt
package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<String> {
        return authRepository.sendPasswordResetEmail(email)
    }
}