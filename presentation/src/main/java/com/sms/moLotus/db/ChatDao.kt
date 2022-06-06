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

    @Query("SELECT * FROM ChatMessageTable WHERE userId=:userId ORDER BY dateSent DESC")
    fun getAllChat(userId: String): LiveData<List<ChatMessage>>

}