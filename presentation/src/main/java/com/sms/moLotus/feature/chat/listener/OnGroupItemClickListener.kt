package com.sms.moLotus.feature.chat.listener

import android.widget.LinearLayout
import com.sms.moLotus.common.widget.QkTextView

interface OnGroupItemClickListener {
    fun onGroupItemClick(id: String, txt: QkTextView, llItem: LinearLayout, position: Int)
}