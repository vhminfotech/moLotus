package com.sms.moLotus.feature.model

data class DeleteMessageInput(
    val threadId: String,
    val userId: String,
    val messageId: String,
)
