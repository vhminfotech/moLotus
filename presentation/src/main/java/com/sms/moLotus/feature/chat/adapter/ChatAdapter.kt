package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.MessageViewHolder
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage

@RequiresApi(Build.VERSION_CODES.M)
class ChatAdapter(
    var list: MutableList<ChatMessage>, context: Context, val listener: OnMessageClickListener
) :
    RecyclerView.Adapter<MessageViewHolder<*>>() {
    companion object {
        const val TYPE_MY_MESSAGE = 0
        const val TYPE_FRIEND_MESSAGE = 1
    }

    var getContext = context

    fun updateList(data: MutableList<ChatMessage>) {
        list.clear()
        list.addAll(data)
        list.sortBy { it.dateSent }
        notifyItemInserted(itemCount - 1)
        notifyDataSetChanged()
    }

    fun deleteMessage(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
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
        when (holder) {
            is MyMessageViewHolder -> holder.bind(list, listener, getContext)
            is FriendMessageViewHolder -> holder.bind(list, listener, getContext)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int = list.size
    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == PreferenceHelper.getStringPreference(
                getContext,
                Constants.USERID
            )
        ) {
            TYPE_MY_MESSAGE
        } else {
            TYPE_FRIEND_MESSAGE
        }
    }

    class MyMessageViewHolder(val view: View) :
        MessageViewHolder<ChatMessage>(view) {
        private val messageContent = view.findViewById<TextView>(R.id.message)
        private val constraintMyMsg = view.findViewById<ConstraintLayout>(R.id.constraintMyMsg)
        private val llOnClick = view.findViewById<LinearLayout>(R.id.llOnClick)

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context
        ) {
            messageContent.text = item?.get(adapterPosition)?.message
            val data = item?.get(adapterPosition)
            constraintMyMsg?.setOnLongClickListener {
                listener.onMessageClick(data, llOnClick, adapterPosition)
                llOnClick.setBackgroundColor(
                    context.resources.getColor(
                        R.color.grey_translucent,
                        context.theme
                    )
                )
                return@setOnLongClickListener true
            }
        }
    }

    class FriendMessageViewHolder(val view: View) :
        MessageViewHolder<ChatMessage>(view) {
        private val messageContent = view.findViewById<TextView>(R.id.message)
        private val constraintFriendMsg =
            view.findViewById<ConstraintLayout>(R.id.constraintFriendMsg)
        private val llOnClick = view.findViewById<LinearLayout>(R.id.llOnClick)

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context
        ) {
            messageContent.text = item?.get(adapterPosition)?.message
            val data = item?.get(adapterPosition)
            constraintFriendMsg?.setOnLongClickListener {
                listener.onMessageClick(data, llOnClick, adapterPosition)
                llOnClick.setBackgroundColor(
                    context.resources.getColor(
                        R.color.grey_translucent,
                        context.theme
                    )
                )
                return@setOnLongClickListener true
            }
        }
    }
}