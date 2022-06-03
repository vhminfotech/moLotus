package com.sms.moLotus.feature.chat.model

data class SocketMessageModel(
    val senderId: String,
    val receiverId: String,
    val message: String,
)
