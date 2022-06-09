package com.sms.moLotus.feature.chat

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage

abstract class MessageViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: List<ChatMessage?>?, listener: OnMessageClickListener, context: Context)
}