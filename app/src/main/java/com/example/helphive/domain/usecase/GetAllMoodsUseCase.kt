package com.example.helphive.domain.usecase

import com.example.helphive.data.model.Mood
import com.example.helphive.domain.repository.MoodRepository
import javax.inject.Inject

class GetAllMoodsUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(): Result<List<Mood>> {
        // This would need to be implemented in the repository
        // Since we don't have a method to get all moods, we'll need to update the repository
        return moodRepository.getAllMoods()
    }
}