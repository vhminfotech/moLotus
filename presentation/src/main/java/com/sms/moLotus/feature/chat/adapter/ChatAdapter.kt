package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.MessageViewHolder
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage


@RequiresApi(Build.VERSION_CODES.M)
class ChatAdapter(
    var list: MutableList<ChatMessage>, context: Context, val listener: OnMessageClickListener,
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
        private val constraintMyMsg = view.findViewById<LinearLayout>(R.id.constraintMyMsg)
        private val llOnClick = view.findViewById<LinearLayout>(R.id.llOnClick)
        private val imgThumbnail = view.findViewById<ImageView>(R.id.imgThumbnail)
        private val llDoc = view.findViewById<LinearLayout>(R.id.llDoc)
        private val txtDocName = view.findViewById<TextView>(R.id.txtDocName)
        private val llContact = view.findViewById<LinearLayout>(R.id.llContact)
        private val txtContactName = view.findViewById<TextView>(R.id.txtContactName)
        private val imgSeen = view.findViewById<ImageView>(R.id.imgSeen)

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context,
        ) {

            val data = item?.get(adapterPosition)
            if (data?.message == "null" || data?.message == "" || data?.message?.isEmpty() == true) {
                messageContent?.visibility = View.GONE
            } else {
                messageContent?.visibility = View.VISIBLE
                messageContent.text = data?.message
            }
            if (data?.url == "null" || data?.url == "" || data?.url?.isEmpty() == true) {
                imgThumbnail?.visibility = View.GONE
            } else {
                if (data?.url?.endsWith(".mp4") == true || data?.url?.endsWith(".3gp") == true) {
                    imgThumbnail?.visibility = View.VISIBLE
                    llDoc?.visibility = View.GONE
                    llContact?.visibility = View.GONE
                    Glide.with(context).asBitmap().load(data.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                } else if (data?.url?.endsWith(".jpg") == true || data?.url?.endsWith(".jpeg") == true || data?.url?.endsWith(
                        ".png"
                    ) == true
                ) {
                    llContact?.visibility = View.GONE
                    imgThumbnail?.visibility = View.VISIBLE
                    llDoc?.visibility = View.GONE
                    Glide.with(context).asBitmap().load(data.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                } else if (data?.url?.endsWith(".vcf") == true) {
                    imgThumbnail?.visibility = View.GONE
                    llDoc?.visibility = View.GONE
                    llContact?.visibility = View.VISIBLE
                    val fileName: String = data.url.substring(data.url.lastIndexOf('/') + 1)
                    txtContactName.text = fileName
                } else {
                    imgThumbnail?.visibility = View.GONE
                    llContact?.visibility = View.GONE
                    llDoc?.visibility = View.VISIBLE
                    val fileName: String? = data?.url?.substring(data.url.lastIndexOf('/') + 1)
                    txtDocName.text = fileName.toString()
                }
            }

            llDoc.setOnClickListener {
                listener.onDocumentClick(data?.url.toString())
            }

            llContact.setOnClickListener {
                listener.onDocumentClick(data?.url.toString())
            }

            imgThumbnail?.setOnClickListener {
                listener.onAttachmentClick(data?.url.toString())
            }

            constraintMyMsg?.setOnClickListener {
                listener.onMessageDeselect()
                llOnClick.setBackgroundColor(
                    context.resources.getColor(
                        android.R.color.transparent,
                        context.theme
                    )
                )
            }
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

            Log.e("CHATACTIVITY", "data: getMessage read adapter: ${data?.read}")
            

            if (data?.read == true){
                imgSeen?.setImageResource(R.drawable.double_tick)
                imgSeen.setColorFilter(ContextCompat.getColor(context, R.color.tools_theme))

            }else{
                imgSeen?.setImageResource(R.drawable.double_tick)
            }
        }
    }

    class FriendMessageViewHolder(val view: View) :
        MessageViewHolder<ChatMessage>(view) {
        private val messageContent = view.findViewById<TextView>(R.id.message)
        private val constraintFriendMsg =
            view.findViewById<ConstraintLayout>(R.id.constraintFriendMsg)
        private val llOnClick = view.findViewById<LinearLayout>(R.id.llOnClick)
        private val txtName = view.findViewById<TextView>(R.id.txtName)
        private val imgThumbnail = view.findViewById<ImageView>(R.id.imgThumbnail)
        private val llDoc = view.findViewById<LinearLayout>(R.id.llDoc)
        private val txtDocName = view.findViewById<TextView>(R.id.txtDocName)
        private val llContact = view.findViewById<LinearLayout>(R.id.llContact)
        private val txtContactName = view.findViewById<TextView>(R.id.txtContactName)

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context,
        ) {
            val data = item?.get(adapterPosition)
            if (data?.message == "null" || data?.message == "" || data?.message?.isEmpty() == true) {
                messageContent?.visibility = View.GONE
            } else {
                messageContent?.visibility = View.VISIBLE
                messageContent.text = data?.message
            }

            messageContent.text = data?.message
            if (data?.url == "null" || data?.url == "" || data?.url?.isEmpty() == true) {
                imgThumbnail?.visibility = View.GONE
            } else {

                if (data?.url?.endsWith(".mp4") == true || data?.url?.endsWith(".3gp") == true) {
                    imgThumbnail?.visibility = View.VISIBLE
                    llDoc?.visibility = View.GONE
                    llContact?.visibility = View.GONE
                    Glide.with(context).asBitmap().load(data.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                } else if (data?.url?.endsWith(".jpg") == true || data?.url?.endsWith(".jpeg") == true || data?.url?.endsWith(
                        ".png"
                    ) == true
                ) {
                    llContact?.visibility = View.GONE
                    imgThumbnail?.visibility = View.VISIBLE
                    llDoc?.visibility = View.GONE
                    Glide.with(context).asBitmap().load(data.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                } else if (data?.url?.endsWith(".vcf") == true) {
                    imgThumbnail?.visibility = View.GONE
                    llDoc?.visibility = View.GONE
                    llContact?.visibility = View.VISIBLE
                    val fileName: String = data.url.substring(data.url.lastIndexOf('/') + 1)
                    txtContactName.text = fileName
                } else {
                    llContact?.visibility = View.GONE
                    imgThumbnail?.visibility = View.GONE
                    llDoc?.visibility = View.VISIBLE
                    val fileName: String? = data?.url?.substring(data.url.lastIndexOf('/') + 1)
                    txtDocName.text = fileName.toString()
                }
            }

            if (!data?.userName.isNullOrEmpty()) {
                txtName.visibility = View.VISIBLE
                txtName.text = data?.userName
            } else {
                txtName.visibility = View.GONE
            }
            constraintFriendMsg?.setOnClickListener {
                listener.onMessageDeselect()
                llOnClick.setBackgroundColor(
                    context.resources.getColor(
                        android.R.color.transparent,
                        context.theme
                    )
                )
            }
            llDoc.setOnClickListener {
                listener.onDocumentClick(data?.url.toString())
            }

            llContact.setOnClickListener {
                listener.onDocumentClick(data?.url.toString())
            }

            imgThumbnail?.setOnClickListener {
                listener.onAttachmentClick(data?.url.toString())
            }
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

    fun onDelivered(delivered: Boolean) {
        Log.e("READ","=OnDelivered== $delivered")

    }

    fun onMarkSeen(markSeen: Boolean) {
        Log.e("READ","=OnMarkSeen== $markSeen")
    }
}