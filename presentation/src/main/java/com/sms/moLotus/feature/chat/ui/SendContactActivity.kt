package com.sms.moLotus.feature.chat.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.apollographql.apollo3.api.DefaultUpload
import com.apollographql.apollo3.api.Upload
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.retrofit.MainViewModel
import kotlinx.android.synthetic.main.activity_send_contact.*

@RequiresApi(Build.VERSION_CODES.M)
class SendContactActivity : AppCompatActivity() {
    var name: String? = ""
    var number: String? = ""
    private var vCard: String? = ""
    var url: String? = ""
    lateinit var viewModel: MainViewModel
    private var recipientsIds: ArrayList<String>? = ArrayList()
    private var threadId: String = ""
    private var fileName = ""
    private var groupName = ""
    private var currentUserId = ""
    private var flag: Boolean? = true
    private var isGroup: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_contact)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        name = intent.getStringExtra("name")
        number = intent.getStringExtra("number")
        vCard = intent.getStringExtra("vCard")
        fileName = intent?.getStringExtra("fileName").toString()
        threadId = intent?.getStringExtra("threadId").toString()
        flag = intent?.getBooleanExtra("flag", false)
        isGroup = intent.getBooleanExtra("isGroup", false)
        groupName = intent?.getStringExtra("groupName").toString()
        recipientsIds = intent?.getStringArrayListExtra("recipientsIds")
        currentUserId = intent?.getStringExtra("currentUserId").toString()
        txtUserName?.text = name.toString()
        txtPhoneNo?.text = number.toString()
        val upload = DefaultUpload.Builder().content(vCard.toString())
            .contentType("text/x-vcard")
            .fileName("$name.vcf").build()
        uploadAttachment(upload)
        setListeners()
    }

    private fun setListeners(){
        imgSend?.setOnClickListener {
            if (flag == true) {
                createThread("", isGroup, groupName, url.toString())
            } else {
                if ((threadId.isEmpty() || threadId == "null")) {
                    createThread("", isGroup, groupName, url.toString())
                } else {
                    if (isGroup) {
                        createMessage(threadId, "", "", url.toString())
                    } else {
                        createMessage(
                            threadId,
                            "",
                            recipientsIds?.get(0).toString(),
                            url.toString()
                        )
                    }
                }
            }
        }

        imgBack?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun uploadAttachment(upload: Upload) {
        viewModel.uploadAttachments.observe(this) {
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
        ChatActivity.mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, ChatActivity.myUserName, url
        )
        viewModel.createThread.observe(this) {
            finish()
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
        ChatActivity.mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, ChatActivity.myUserName, url
        )
        viewModel.createMessage.observe(this) {
            finish()
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