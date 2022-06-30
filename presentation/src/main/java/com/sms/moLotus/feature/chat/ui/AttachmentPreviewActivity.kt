package com.sms.moLotus.feature.chat.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.apollographql.apollo3.api.DefaultUpload
import com.apollographql.apollo3.api.Upload
import com.apollographql.apollo3.api.content
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.LogHelper
import com.sms.moLotus.feature.retrofit.MainViewModel
import kotlinx.android.synthetic.main.activity_attachment_preview.*
import timber.log.Timber
import java.io.File

@RequiresApi(Build.VERSION_CODES.M)
class AttachmentPreviewActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private var recipientsIds: ArrayList<String>? = ArrayList()
    private var threadId: String = ""
    private var fileName = ""
    private var fileType = ""
    private var groupName = ""
    private var currentUserId = ""
    private var flag: Boolean? = true
    private var url: String = ""
    private var isGroup: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment_preview)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        fileName = intent?.getStringExtra("fileName").toString()
        fileType = intent?.getStringExtra("fileType").toString()
        threadId = intent?.getStringExtra("threadId").toString()
        flag = intent?.getBooleanExtra("flag", false)
        isGroup = intent.getBooleanExtra("isGroup", false)
        groupName = intent?.getStringExtra("groupName").toString()
        recipientsIds = intent?.getStringArrayListExtra("recipientsIds")
        currentUserId = intent?.getStringExtra("currentUserId").toString()

        LogHelper.e("=============", "Filename:: $fileName::::Filetype:: $fileType")
        LogHelper.e("=============", "Filename:: ${File(fileName).name}")
        LogHelper.e(
            "=============",
            "currentUserId:: $currentUserId == threadId:: $threadId == flag:: $flag == groupName:: $groupName == isGroup:: $isGroup == recipientsIds:: $recipientsIds"
        )

        var contentType = ""

        if (fileType.uppercase() == "IMAGE") {
            contentType = "image/*"
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            imgPlay?.visibility = View.GONE
            Glide.with(this).load(fileName).into(imageView)
        } else {
            contentType = "video/*"
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

        val upload = DefaultUpload.Builder().content(File(fileName))
            .contentType(contentType)
            .fileName(File(fileName).name).build()
        uploadAttachment(upload)

        setListeners()
    }

    private fun setListeners() {
        imgClose?.setOnClickListener {
            onBackPressed()
        }

        imgSend.setOnClickListener {
            //getMessage list empty then create thread else create message
            Log.e("=====", "url== $url")
            if (flag == true) {
                createThread(txtAddCaption.text.toString(), isGroup, groupName, url)
            } else {
                if ((threadId.isEmpty() || threadId == "null")) {
                    Log.e("=====", "createThread")
                    createThread(txtAddCaption.text.toString(), isGroup, groupName, url)
                } else {
                    Log.e("=====", "createMessage : $threadId")
                    Log.e("=====", "isGroup : $isGroup")
                    Log.e("=====", "recipientsid : ${recipientsIds?.get(0).toString()}")

                    if (isGroup) {
                        createMessage(threadId, txtAddCaption.text.toString(), "", url)
                    } else {
                        createMessage(
                            threadId,
                            txtAddCaption.text.toString(),
                            recipientsIds?.get(0).toString(),
                            url
                        )
                    }
                }
            }
        }
    }

    private fun uploadAttachment(upload: Upload) {
        viewModel.uploadAttachments.observe(this) {
            LogHelper.e("======================", "uploadAttachments:: ${it.uploadAttachments}")
            url = it.uploadAttachments?.uri.toString()
        }
        viewModel.errorMessage.observe(this) {
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.relMain),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {

                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }

        viewModel.uploadAttachments(upload)
    }


    private fun createThread(message: String, isGroup: Boolean, groupName: String, url: String) {

        viewModel.createThread.observe(this) {
            LogHelper.e("======================", "createThread:: ${it.createThread?.id}")
            LogHelper.e("======================", "isGroup:: $isGroup")
            txtAddCaption.text = null
            onBackPressed()
        }
        viewModel.errorMessage.observe(this) {
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.relMain),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                ).setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }
        recipientsIds?.let {
            viewModel.createThread(
                message, currentUserId,
                it, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString(),
                isGroup, groupName, url
            )
        }


    }

    private fun createMessage(threadId: String, message: String, receiverId: String, url: String) {
        Timber.e("recipientsIds:: $recipientsIds")
        Timber.e("receiverId:: $receiverId")

        viewModel.createMessage.observe(this) {
            Timber.e("createMessage:: $it")
            txtAddCaption.text = null
            onBackPressed()
        }
        viewModel.errorMessage.observe(this) {
            val conMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.relMain),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {

                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }

        viewModel.createMessage(
            message,
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString(), url, receiverId,
        )
    }
}