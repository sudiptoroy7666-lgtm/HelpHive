// File: com/example/helphive/domain/usecase/DeleteHelpRequestUseCase.kt
package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.HelpRepository
import javax.inject.Inject

class DeleteHelpRequestUseCase @Inject constructor(
    private val helpRepository: HelpRepository
) {
    suspend operator fun invoke(requestId: String): Result<String> {
        return helpRepository.deleteHelpRequest(requestId)
    }
}