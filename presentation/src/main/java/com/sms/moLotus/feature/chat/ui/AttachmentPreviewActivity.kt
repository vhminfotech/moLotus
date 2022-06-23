package com.sms.moLotus.feature.chat.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sms.moLotus.R
import com.sms.moLotus.feature.chat.LogHelper
import kotlinx.android.synthetic.main.activity_attachment_preview.*

class AttachmentPreviewActivity : AppCompatActivity() {
    var fileName = ""
    var fileType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment_preview)
        fileName = intent?.getStringExtra("fileName").toString()
        fileType = intent?.getStringExtra("fileType").toString()

        LogHelper.e("=============", "Filename:: $fileName::::Filetype:: $fileType")

        if (fileType.uppercase() == "IMAGE") {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            imgPlay?.visibility = View.GONE
            Glide.with(this).load(fileName).into(imageView)
        } else {
            videoView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            videoView.setVideoURI(Uri.parse(fileName))
            imgPlay?.visibility = View.VISIBLE

            if (videoView.isPlaying) {
                imgPlay?.visibility = View.GONE
            } else {
                imgPlay?.visibility = View.VISIBLE
            }

            videoView?.setOnCompletionListener {
                imgPlay?.visibility = View.VISIBLE
            }


            imgPlay?.setOnClickListener {
                videoView?.start()
                imgPlay?.visibility = View.GONE

            }

            videoView?.setOnClickListener {
                imgPlay?.visibility = View.VISIBLE
                videoView?.pause()
            }
        }

        imgClose?.setOnClickListener {
            onBackPressed()
        }

    }
}