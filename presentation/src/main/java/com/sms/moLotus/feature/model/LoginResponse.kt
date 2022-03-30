package com.sms.moLotus.feature.model

data class LoginResponse(
    val status: Int,
    val user_id: String,
    val message: String,
    val user_data: ArrayList<UserData>,
    val token: String
)