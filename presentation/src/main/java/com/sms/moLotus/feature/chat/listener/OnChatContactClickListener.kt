package com.sms.moLotus.feature.chat.listener

import com.sms.moLotus.GetUserUsingAppQuery

interface OnChatContactClickListener {
    fun onChatContactClick(item: GetUserUsingAppQuery.UserDatum?)
}