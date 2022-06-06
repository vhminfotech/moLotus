package com.sms.moLotus.repository

import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.feature.chat.model.ChatMessage


class ChatRepository(val db: ChatDatabase) {

    suspend fun insert(message: ChatMessage) =
        db.getChatDao().insert(message)


    suspend fun insertAllMessages(list: List<ChatMessage>) =
        db.getChatDao().insertAllMessages(list)


    fun getAllChat(id: String) = db.getChatDao().getAllChat(id)

}