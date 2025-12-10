package com.example.helphive.domain.repository

import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.model.Mood
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService
) : MoodRepository {

    override suspend fun addMood(mood: Mood): Result<String> {
        return firestoreService.addMood(mood)
    }

    override suspend fun getUserMoods(userId: String): Result<List<Mood>> {
        return firestoreService.getUserMoods(userId)
    }

    override suspend fun getAllMoods(): Result<List<Mood>> {
        return firestoreService.getAllMoods()  // This needs to be implemented in FirestoreService
    }
}