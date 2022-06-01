package com.sms.moLotus.feature.chat.listener

import com.sms.moLotus.GetThreadListQuery

interface OnItemClickListener {
    fun onItemClick(item: GetThreadListQuery.RecipientUser?)
}