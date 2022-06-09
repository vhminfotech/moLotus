package com.sms.moLotus.feature.chat.listener

import android.widget.LinearLayout
import com.sms.moLotus.feature.chat.model.ChatMessage

interface OnMessageClickListener {
    fun onMessageClick(item: List<ChatMessage?>?, llOnClick: LinearLayout)
}