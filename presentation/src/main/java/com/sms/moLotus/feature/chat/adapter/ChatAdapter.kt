package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.MessageViewHolder
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage
import java.io.IOException
import java.io.InputStream
import java.net.URL


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
        //notifyItemInserted(list.size)
        //  notifyItemRangeInserted(list.size, itemCount)

        /*list?.sortByDescending { it.dateSent }
        notifyItemInserted(list.size - 1)
        notifyItemRangeInserted(list.size - 1, list.size)*/
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
        private val imgThumbnail = view.findViewById<ImageView>(R.id.imgThumbnail)

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context
        ) {

            val data = item?.get(bindingAdapterPosition)
            Log.e("=========", "url :: ${data?.url}")
            if (data?.message == "null" || data?.message == "" || data?.message?.isEmpty() == true) {
                messageContent?.visibility = View.GONE
            } else {
                messageContent?.visibility = View.VISIBLE
                messageContent.text = data?.message
            }
            if (data?.url == "null" || data?.url == "" || data?.url?.isEmpty() == true) {
                imgThumbnail?.visibility = View.GONE
            } else {
                imgThumbnail?.visibility = View.VISIBLE
                /*GlobalScope.launch(Dispatchers.IO){
                    val bitmap = Utils.getBitmapFromURL(data?.url)
                    Log.e("==============", "bitmap--" + bitmap)
                }*/
                Glide.with(context).asBitmap().load(data?.url)
                    .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                /*if (data?.url?.endsWith(".mp4") == true || data?.url?.endsWith(".3gp") == true) {
                    GlobalScope.launch(Dispatchers.IO) {
                       val  fileName = Utils.getBitmapFromURL(data.url, context).toString()
                        Log.e("===========","fileName = $fileName")
                        GlobalScope.launch(Dispatchers.Main){


                                imgThumbnail.setImageBitmap(
                                    ThumbnailUtils.createVideoThumbnail(
                                        fileName,
                                        MediaStore.Images.Thumbnails.MICRO_KIND
                                    )
                                )

                        }
                    }


                } else {

                }*/
                // Glide.with(context).load(data?.url).into(imgThumbnail)

            }

            imgThumbnail?.setOnClickListener {
                listener.onAttachmentClick(data?.url)
            }

            constraintMyMsg?.setOnClickListener {
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

        override fun bind(
            item: List<ChatMessage?>?,
            listener: OnMessageClickListener,
            context: Context
        ) {
            val data = item?.get(adapterPosition)
            if (data?.message == "null" || data?.message == "" || data?.message?.isEmpty() == true) {
                messageContent?.visibility = View.GONE
            } else {
                messageContent?.visibility = View.VISIBLE
                messageContent.text = data?.message
            }

            Log.e("=========", "userName :: ${data?.userName}")
            Log.e("=========", "url :: ${data?.url}")
            messageContent.text = data?.message
            if (data?.url == "null" || data?.url == "" || data?.url?.isEmpty() == true) {
                imgThumbnail?.visibility = View.GONE
            } else {
                imgThumbnail?.visibility = View.VISIBLE
                Glide.with(context).asBitmap().load(data?.url)
                    .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
               /* if (data?.url?.endsWith(".mp4") == true || data?.url?.endsWith(".3gp") == true) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val fileName = Utils.getBitmapFromURL(data.url, context).toString()
                        GlobalScope.launch(Dispatchers.Main) {
                            Log.e("===========", "fileName = $fileName")

                            imgThumbnail.setImageBitmap(
                                ThumbnailUtils.createVideoThumbnail(
                                    fileName,
                                    MediaStore.Images.Thumbnails.MICRO_KIND
                                )
                            )

                        }
                    }
                } else {
                    Glide.with(context).asBitmap().load(data?.url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(imgThumbnail)
                }*/
            }



            if (!data?.userName.isNullOrEmpty()) {
                txtName.visibility = View.VISIBLE
                txtName.text = data?.userName
            } else {
                txtName.visibility = View.GONE
            }
            constraintFriendMsg?.setOnClickListener {
                llOnClick.setBackgroundColor(
                    context.resources.getColor(
                        android.R.color.transparent,
                        context.theme
                    )
                )
            }
            imgThumbnail?.setOnClickListener {
                listener.onAttachmentClick(data?.url)
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


    class GetImageFromUrl(img: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        var bitmap: Bitmap? = null
        var imageView: ImageView? = null
        override fun doInBackground(vararg url: String?): Bitmap? {
            val stringUrl = url[0]
            bitmap = null
            val inputStream: InputStream
            try {
                inputStream = URL(stringUrl).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                Log.e("=========", "error :: ${e.message}")
                e.printStackTrace()
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            Log.e("=========", "bitmap :: $bitmap")
            imageView?.setImageBitmap(bitmap)
        }

        init {
            imageView = img
        }

    }
}