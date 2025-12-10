package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.HelpRepository
import com.example.helphive.data.model.HelpRequest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetHelpRequestsUseCase @Inject constructor(
    private val helpRepository: HelpRepository
) {
    suspend operator fun invoke(): Result<List<HelpRequest>> {
        android.util.Log.d(
            "GetHelpRequestsUseCase",
            "Calling helpRepository.getHelpRequests"
        ) // Debug log
        val result = helpRepository.getHelpRequests()
        android.util.Log.d(
            "GetHelpRequestsUseCase",
            "Repository returned result with ${result.getOrNull()?.size ?: "error"} items"
        ) // Debug log
        return result
    }
}