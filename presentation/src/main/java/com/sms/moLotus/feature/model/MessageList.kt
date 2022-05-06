package com.sms.moLotus.feature.model

data class MessageList(
    val id: Int,
    val last_sender_id: Int,
    val message: String,
    val date: String,
    val unread_count: Int = 0,
    val recipients_ids: ArrayList<Int>,
    val current_user: Int = 0,
    val recipient_user: ArrayList<UserData>,
    val is_group: Boolean = false,
    val group_name: String? = null,
    val group_avatar: String? = null,
    val recipients_count: Int = 0,
    val messages: ArrayList<Message>
)
