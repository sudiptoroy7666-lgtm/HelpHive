package com.example.helphive.domain.usecase


import com.example.helphive.data.model.Mood
import com.example.helphive.domain.repository.MoodRepository
import javax.inject.Inject

class GetUserMoodsUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Mood>> {
        return moodRepository.getUserMoods(userId)
    }
}