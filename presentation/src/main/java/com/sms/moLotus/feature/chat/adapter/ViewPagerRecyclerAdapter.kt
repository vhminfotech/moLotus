package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sms.moLotus.R


class ViewPagerRecyclerAdapter(
    var context: Context
) :
    RecyclerView.Adapter<ViewPagerRecyclerAdapter.ViewHolder>() {
    var arrayList: ArrayList<String> = arrayListOf()


    fun setItem(list: ArrayList<String>) {
        this.arrayList = list
        notifyDataSetChanged()
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_media_preview, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (arrayList?.get(position)?.endsWith(".mp4") == true || arrayList?.get(position)
                ?.endsWith(".3gp") == true
        ) {

            if (holder.videoView.isPlaying) {
                holder.imgPlay.visibility = View.GONE
            } else {
                holder.imgPlay.visibility = View.VISIBLE
            }

            holder.videoView.setOnCompletionListener {
                holder.imgPlay.visibility = View.VISIBLE
            }


            holder.imgPlay.setOnClickListener {
                holder.videoView.start()
                holder.imgPlay.visibility = View.GONE

            }

            holder.videoView.setOnClickListener {
                holder.imgPlay.visibility = View.VISIBLE
                holder.videoView.pause()
            }
        } else {
            Glide.with(context).load(arrayList?.get(position)).into(holder.imageView)
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView =
            itemView.findViewById(R.id.imageView) as ImageView
        val imgPlay: ImageView =
            itemView.findViewById(R.id.imgPlay) as ImageView

        val videoView: VideoView =
            itemView.findViewById(R.id.videoView) as VideoView
    }
}