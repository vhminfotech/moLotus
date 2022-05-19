package com.sms.moLotus.repository

import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.entity.Message


class ChatRepository(val db: ChatDatabase) {

    suspend fun insert(message: Message) =
        db.getArticleDao().insert(message)


    fun getAllChat(id: String) = db.getArticleDao().getAllChat(id)

}