package com.sms.moLotus.feature.model

data class UserData(
    val id: Int,
    val name: String,
    val operator: Int,
    val MSISDN: String,
    val chat_feature: Int,
    val user_status: Int,
    val last_active: String,
    val created_at: String,
    val updated_at: String,
)
