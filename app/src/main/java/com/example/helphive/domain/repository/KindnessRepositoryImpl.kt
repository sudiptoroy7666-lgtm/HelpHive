package com.example.helphive.domain.repository

import com.example.helphive.core.utils.OfflineCacheManager
import com.example.helphive.data.firebase.FirestoreService
import com.example.helphive.data.model.Kindness
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class KindnessRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val cacheManager: OfflineCacheManager
) : KindnessRepository {

    override suspend fun addKindness(kindness: Kindness): Result<String> {
        val res = firestoreService.addKindness(kindness)
        return if (res.isSuccess) {
            // update cache (best effort)
            cacheManager.addToList("kindness_feed", kindness)
            res
        } else {
            res
        }
    }


    override suspend fun getKindnessFeed(): Result<List<Kindness>> {
        val cacheKey = "kindness_feed"
        val res = firestoreService.getKindnessFeed()
        return if (res.isSuccess) {
            val feed = res.getOrThrow()
            // cache (best effort)
            cacheManager.saveData(cacheKey, feed)
            feed.forEach { k ->
                if (k.userId.isNotEmpty()) {
                    cacheManager.cacheUser(k.userId, k.userName, k.userProfileImage)
                }
            }
            Result.success(feed)
        } else {
            // fallback to cached copy
            val cachedFeed = cacheManager.getData<List<Kindness>>(cacheKey, null)
            if (!cachedFeed.isNullOrEmpty()) Result.success(cachedFeed)
            else Result.failure(res.exceptionOrNull() ?: Exception("Failed to load"))
        }
    }

}