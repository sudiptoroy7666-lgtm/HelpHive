package com.example.helphive.domain.usecase

import com.example.helphive.domain.repository.KindnessRepository
import com.example.helphive.data.model.Kindness
import javax.inject.Inject

class GetKindnessFeedUseCase @Inject constructor(
    private val kindnessRepository: KindnessRepository
) {
    suspend operator fun invoke(): Result<List<Kindness>> {
        return kindnessRepository.getKindnessFeed()
    }
}