package com.sms.moLotus.feature.chat.listener

interface OnReadReceiptListener {
    fun onDelivered(delivered: Boolean)
    fun onMarkSeen(markSeen: Boolean)
}