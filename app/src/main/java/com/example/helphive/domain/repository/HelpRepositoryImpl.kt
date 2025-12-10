package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.model.HelpRequest

import javax.inject.Inject

class HelpRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : HelpRepository {

    override suspend fun addHelpRequest(helpRequest: HelpRequest): Result<String> {
        return firestoreService.addHelpRequest(helpRequest)
    }

    override suspend fun getHelpRequests(): Result<List<HelpRequest>> {
        android.util.Log.d("HelpRepositoryImpl", "Delegating getHelpRequests to FirestoreService") // Debug log
        val result = firestoreService.getHelpRequests()
        android.util.Log.d("HelpRepositoryImpl", "FirestoreService returned result with ${result.getOrNull()?.size ?: "error"} items") // Debug log
        return result
    }

    override suspend fun updateHelpRequest(helpRequest: HelpRequest): Result<String> {
        return firestoreService.updateHelpRequest(helpRequest)
    }

    override suspend fun deleteHelpRequest(requestId: String): Result<String> {
        return firestoreService.deleteHelpRequest(requestId)
    }
}