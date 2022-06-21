package com.sms.moLotus.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.chat.model.Users

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllUsers(users: Users)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(order: List<ChatMessage?>?)

    @Query("SELECT * FROM ChatMessageTable WHERE threadId=:threadId ORDER BY dateSent DESC")
    fun getAllChat(threadId: String): LiveData<List<ChatMessage>>

    @Query("SELECT * FROM UsersTable WHERE myUserId=:id")
    fun getAllUsers(id: String): LiveData<List<Users>>

    @Query("DELETE FROM ChatMessageTable")
    fun deleteTable()

    @Query("DELETE FROM UsersTable")
    fun deleteUsersTable()

    @Query("DELETE FROM ChatMessageTable WHERE id in (:messageId)")
    fun deleteMessage(messageId: List<String>)

    @Query("DELETE FROM ChatMessageTable WHERE threadId in (:threadId)")
    fun deleteAllMessages(threadId: List<String>)
}