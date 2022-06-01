package com.sms.moLotus.feature.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.GetMessageListQuery

abstract class MessageViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: List<GetMessageListQuery.Message?>?)
}