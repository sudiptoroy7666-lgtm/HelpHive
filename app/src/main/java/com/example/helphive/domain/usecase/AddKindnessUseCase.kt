package com.example.helphive.domain.usecase


import com.example.helphive.data.model.Kindness
import com.example.helphive.domain.repository.KindnessRepository
import javax.inject.Inject

class AddKindnessUseCase @Inject constructor(
    private val kindnessRepository: KindnessRepository
) {
    suspend operator fun invoke(kindness: Kindness): Result<String> {
        return kindnessRepository.addKindness(kindness)
    }
}