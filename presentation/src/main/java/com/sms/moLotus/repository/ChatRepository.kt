package com.sms.moLotus.repository

import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.chat.model.Users


class ChatRepository(val db: ChatDatabase) {

    suspend fun insert(message: ChatMessage) =
        db.getChatDao().insert(message)

    suspend fun insertAllUsers(users: Users) =
        db.getChatDao().insertAllUsers(users)

    suspend fun updateThreadId(id: String, myId: String, userId: String) =
        db.getChatDao().updateThreadId(id, myId, userId)


    suspend fun insertAllMessages(list: List<ChatMessage>) =
        db.getChatDao().insertAllMessages(list)


    fun getAllChat(id: String) = db.getChatDao().getAllChat(id)
    fun getAllUsers(id: String) = db.getChatDao().getAllUsers(id)
    fun clearThreadId(idList: List<String>) = db.getChatDao().clearThreadId(idList)

    fun deleteMessage(id: List<String>) = db.getChatDao().deleteMessage(id)
    fun deleteAllMessages(id: List<String>) = db.getChatDao().deleteAllMessages(id)

    fun deleteTable() = db.getChatDao().deleteTable()
    fun deleteUsersTable() = db.getChatDao().deleteUsersTable()

}