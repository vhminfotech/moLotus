package com.sms.moLotus.feature.chat.listener

import com.sms.moLotus.feature.chat.model.Users

interface OnChatContactClickListener {
    fun onChatContactClick(item: Users?)
    fun onCheckClick(item: Users?, itemRemove: Users?)
}