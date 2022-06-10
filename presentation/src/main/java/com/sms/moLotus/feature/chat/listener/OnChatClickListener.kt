package com.sms.moLotus.feature.chat.listener

import androidx.constraintlayout.widget.ConstraintLayout
import com.sms.moLotus.GetThreadListQuery

interface OnChatClickListener {
    fun onChatClick(item: GetThreadListQuery.RecipientUser?, llOnClick: ConstraintLayout, adapterPosition: Int)
}