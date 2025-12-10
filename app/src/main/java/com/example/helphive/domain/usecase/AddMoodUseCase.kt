package com.example.helphive.domain.usecase


import com.example.helphive.data.model.Mood
import com.example.helphive.domain.repository.MoodRepository
import javax.inject.Inject

class AddMoodUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(mood: Mood): Result<String> {
        return moodRepository.addMood(mood)
    }
}