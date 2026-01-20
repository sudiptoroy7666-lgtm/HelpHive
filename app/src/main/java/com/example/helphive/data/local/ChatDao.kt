package com.example.helphive.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_conversations ORDER BY lastMessageTime DESC")
    fun getConversations(): Flow<List<ChatConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ChatConversationEntity>)

    @Query("DELETE FROM chat_conversations")
    suspend fun clearConversations()
}
