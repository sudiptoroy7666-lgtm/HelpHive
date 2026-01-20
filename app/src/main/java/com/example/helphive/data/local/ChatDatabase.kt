package com.example.helphive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChatConversationEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
