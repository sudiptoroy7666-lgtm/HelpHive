package com.example.helphive.di

import com.example.helphive.core.utils.OfflineCacheManager
import com.example.helphive.domain.repository.*
import com.example.helphive.domain.repository.ChatRepositoryImpl
import com.example.helphive.domain.repository.HelpRepositoryImpl
import com.example.helphive.domain.repository.KindnessRepositoryImpl
import com.example.helphive.domain.repository.MoodRepositoryImpl
import com.example.helphive.domain.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHelpRepository(
        impl: HelpRepositoryImpl
    ): HelpRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(
        impl: MoodRepositoryImpl
    ): MoodRepository

    @Binds
    @Singleton
    abstract fun bindKindnessRepository(
        impl: KindnessRepositoryImpl
    ): KindnessRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: com.example.helphive.data.repository.AuthRepositoryImpl
    ): com.example.helphive.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: com.example.helphive.domain.repository.UserRepositoryImpl
    ): com.example.helphive.domain.repository.UserRepository
}