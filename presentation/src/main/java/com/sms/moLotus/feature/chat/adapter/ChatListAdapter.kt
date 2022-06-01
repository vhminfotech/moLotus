package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.GetThreadListQuery
import com.sms.moLotus.R
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.feature.Utils
import com.sms.moLotus.feature.chat.listener.OnItemClickListener

class ChatListAdapter(
    private val context: Context,
    private val mList: GetThreadListQuery.GetThreadList,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatList = mList.recipientUser?.get(position)
        holder.txtUserName.text = chatList?.name
        holder.txtLastMessage.text = chatList?.message
        holder.txtDate.text = Utils.covertTimeToText(chatList?.messageDate.toString())
        Log.e("==========", "chatList:: $chatList")

        holder.constraintLayout.setOnClickListener {
            listener.onItemClick(chatList)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.recipientUser?.size?:0
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtUserName: QkTextView = itemView.findViewById(R.id.txtUserName)
        val txtLastMessage: QkTextView = itemView.findViewById(R.id.txtLastMessage)
        val txtDate: QkTextView = itemView.findViewById(R.id.txtDate)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.itemClick)
    }
}