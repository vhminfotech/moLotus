package com.sms.moLotus.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sms.moLotus.feature.chat.model.ChatMessage

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(order: List<ChatMessage?>?)

    @Query("SELECT * FROM ChatMessageTable WHERE threadId=:threadId ORDER BY dateSent DESC")
    fun getAllChat(threadId: String): LiveData<List<ChatMessage>>

    @Query("DELETE FROM ChatMessageTable")
    fun deleteTable()

    @Query("DELETE FROM ChatMessageTable WHERE id=:messageId")
    fun deleteMessage(messageId: List<String>)
}