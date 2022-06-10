package com.sms.moLotus.feature.chat.listener

import android.widget.LinearLayout
import com.sms.moLotus.feature.chat.model.ChatMessage

interface OnMessageClickListener {
    fun onMessageClick(item: ChatMessage?, llOnClick: LinearLayout, adapterPosition: Int)
}