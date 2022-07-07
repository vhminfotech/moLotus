package com.sms.moLotus.feature.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.sms.moLotus.R

class ViewPagerAdapter(var context: Context, arrayList: ArrayList<String>?) :
    PagerAdapter() {
    var layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var arrayList: ArrayList<String>?
    override fun getCount(): Int {
        return arrayList?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView: View =
            layoutInflater.inflate(R.layout.layout_media_preview, container, false)
        val imageView: ImageView =
            itemView.findViewById(R.id.imageView) as ImageView
        val imgPlay: ImageView =
            itemView.findViewById(R.id.imgPlay) as ImageView

        val videoView: VideoView =
            itemView.findViewById(R.id.videoView) as VideoView
        if (arrayList?.get(position)?.endsWith(".mp4") == true || arrayList?.get(position)
                ?.endsWith(".3gp") == true
        ) {

            if (videoView.isPlaying) {
                imgPlay.visibility = View.GONE
            } else {
                imgPlay.visibility = View.VISIBLE
            }

            videoView.setOnCompletionListener {
                imgPlay.visibility = View.VISIBLE
            }


            imgPlay.setOnClickListener {
                videoView.start()
                imgPlay.visibility = View.GONE

            }

            videoView.setOnClickListener {
                imgPlay.visibility = View.VISIBLE
                videoView.pause()
            }
        } else {
            Glide.with(context).load(arrayList?.get(position)).into(imageView)
        }

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    init {
        this.arrayList = arrayList
    }
}