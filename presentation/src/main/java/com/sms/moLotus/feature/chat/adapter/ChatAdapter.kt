package com.sms.moLotus.feature.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.feature.chat.MessageViewHolder
import com.sms.moLotus.feature.model.Message

class ChatAdapter(var currentUserId: String, var data: MutableList<Message>) :
    RecyclerView.Adapter<MessageViewHolder<*>>() {
    companion object {
        const val TYPE_MY_MESSAGE = 0
        const val TYPE_FRIEND_MESSAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder<*> {
        val context = parent.context
        return when (viewType) {
            TYPE_MY_MESSAGE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.my_bubble_shape, parent, false)
                MyMessageViewHolder(view)
            }
            TYPE_FRIEND_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.friend_bubble_shape, parent, false)
                FriendMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder<*>, position: Int) {
        val item = data[position]
        Log.d("adapter View", position.toString() + item.message)
        when (holder) {
            is MyMessageViewHolder -> holder.bind(item)
            is FriendMessageViewHolder -> holder.bind(item)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int = data.size
    override fun getItemViewType(position: Int): Int {
        Log.e("=====", "currentUserId adapter:: $currentUserId")
        Log.e("=====", "senderID adapter:: ${data[position].sender_id}")

        return if (data[position].sender_id.equals(currentUserId)) {
            TYPE_MY_MESSAGE
        } else {
            TYPE_FRIEND_MESSAGE
        }
    }

    class MyMessageViewHolder(val view: View) : MessageViewHolder<Message>(view) {
        private val messageContent = view.findViewById<TextView>(R.id.message)

        override fun bind(item: Message) {
            messageContent.text = item.message
        }
    }

    class FriendMessageViewHolder(val view: View) : MessageViewHolder<Message>(view) {
        private val messageContent = view.findViewById<TextView>(R.id.message)

        override fun bind(item: Message) {
            messageContent.text = item.message
        }
    }
}