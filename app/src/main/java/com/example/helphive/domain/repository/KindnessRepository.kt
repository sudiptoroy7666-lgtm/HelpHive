package com.example.helphive.domain.repository


import com.example.helphive.data.model.Kindness

interface KindnessRepository {
    suspend fun addKindness(kindness: Kindness): Result<String>
    suspend fun getKindnessFeed(): Result<List<Kindness>>
}