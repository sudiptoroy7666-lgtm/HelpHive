package com.example.helphive.domain.usecase


import com.example.helphive.data.model.HelpRequest
import com.example.helphive.domain.repository.HelpRepository
import javax.inject.Inject

class AddHelpRequestUseCase @Inject constructor(
    private val helpRepository: HelpRepository
) {
    suspend operator fun invoke(helpRequest: HelpRequest): Result<String> {
        return helpRepository.addHelpRequest(helpRequest)
    }
}