package com.sms.moLotus.feature.chat.model

data class ChatMessage(
    val senderId: String,
    val threadId: String,
    val message: String,
    val dateSent: String,
    val id: String,
)
