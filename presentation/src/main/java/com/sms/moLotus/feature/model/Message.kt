package com.sms.moLotus.feature.model

data class Message(
    val id: Int,
    val thread_id: Int,
    val sender_id: Int,
    val message: String,
    val date_sent: String,
    val is_attachment: Boolean = false,
    val attachment_id: Int,
    val url: String
)
