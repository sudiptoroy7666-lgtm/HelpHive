package com.example.helphive.domain.repository

import com.example.helphive.data.model.HelpRequest

interface HelpRepository {
    suspend fun addHelpRequest(helpRequest: HelpRequest): Result<String>
    suspend fun getHelpRequests(): Result<List<HelpRequest>>
    suspend fun updateHelpRequest(helpRequest: HelpRequest): Result<String>
    suspend fun deleteHelpRequest(requestId: String): Result<String>
}