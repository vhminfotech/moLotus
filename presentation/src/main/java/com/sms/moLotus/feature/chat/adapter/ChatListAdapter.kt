package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.GetThreadListQuery
import com.sms.moLotus.R
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.feature.Utils
import com.sms.moLotus.feature.chat.listener.OnChatClickListener
import com.sms.moLotus.feature.chat.listener.OnItemClickListener

class ChatListAdapter(
    private val context: Context,
    private val mList: GetThreadListQuery.GetThreadList,
    private val listener: OnItemClickListener,
    private val onChatClickListener: OnChatClickListener,
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
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatList = mList.recipientUser?.get(position)

        if (chatList?.isGroup == true || chatList?.name.isNullOrEmpty() || chatList?.name == "null") {
            holder.txtUserName.text = chatList?.groupName
            holder.avatars.setImageDrawable(context.getDrawable(R.drawable.ic_chat_group))
        } else {
            holder.txtUserName.text = chatList?.name
            holder.avatars.setImageDrawable(context.getDrawable(R.drawable.ic_chat_user))
        }
        holder.txtDate.text = Utils.covertTimeToText(chatList?.messageDate.toString())

        holder.constraintLayout.setOnClickListener {
            listener.onItemClick(chatList)
        }

        if (chatList?.message?.isNotEmpty() == true) {
            holder.txtLastMessage.text = chatList.message
        } else {
            if (chatList?.url?.endsWith(".mp4") == true || chatList?.url?.endsWith(".3gp") == true) {
                holder.txtLastMessage.text = "Video"
            } else if (chatList?.url?.endsWith(".jpg") == true || chatList?.url?.endsWith(".jpeg") == true || chatList?.url?.endsWith(
                    ".png"
                ) == true
            ) {
                holder.txtLastMessage.text = "Image"
            } else if (chatList?.url?.endsWith(".vcf") == true) {
                holder.txtLastMessage.text = "Contact"
            } else {
                holder.txtLastMessage.text = "Document"
            }
        }

        if(chatList?.isMuted == true){
            holder.imgMute.visibility = View.VISIBLE
        }else{
            holder.imgMute.visibility = View.GONE

        }


        holder.constraintLayout.setOnLongClickListener {
            onChatClickListener.onChatClick(chatList, holder.constraintLayout, position)
            holder.constraintLayout.setBackgroundColor(
                context.resources.getColor(
                    R.color.grey_translucent,
                    context.theme
                )
            )
            return@setOnLongClickListener true
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.recipientUser?.size ?: 0
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val txtUserName: QkTextView = itemView.findViewById(R.id.txtUserName)
        val txtLastMessage: QkTextView = itemView.findViewById(R.id.txtLastMessage)
        val txtDate: QkTextView = itemView.findViewById(R.id.txtDate)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.itemClick)
        val avatars: ImageView = itemView.findViewById(R.id.avatars)
        val imgMute: ImageView = itemView.findViewById(R.id.mute)
    }
}