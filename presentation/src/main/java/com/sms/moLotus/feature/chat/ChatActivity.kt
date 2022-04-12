package com.sms.moLotus.feature.chat

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.ChatAdapter
import com.sms.moLotus.feature.model.Message
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.layout_header.*

class ChatActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    var messageList: ArrayList<Message> = ArrayList()
    private var chatAdapter: ChatAdapter? = null
    private var currentUserId: Int = 0
    private var threadId: Int = 0
    var userName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        currentUserId = intent?.getIntExtra("currentUser", 0) ?: 0
        threadId = intent?.getIntExtra("threadId", 0) ?: 0
        userName = intent?.getStringExtra("userName").toString()
        txtTitle?.text = userName
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )
        getChatList()

    }

    private fun subscribeOnAddMessage() {
        /*viewModel.notifyNewMessageInsertedLiveData.observe(this, Observer {
            chatAdapter.notifyItemInserted(it)
        })*/
    }

    private fun initRecyclerView(list: ArrayList<Message>) {
        val layoutMgr = LinearLayoutManager(this)
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        Log.e("=====", "currentUserId:: $currentUserId")

        chatAdapter = ChatAdapter(currentUserId, list)
        rvMessageList.adapter = chatAdapter
        chatAdapter?.notifyDataSetChanged()
    }


    private fun getChatList() {
        viewModel.allMessages.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)

            messageList = it.messages
            initRecyclerView(messageList)
        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
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
        })
        viewModel.getAllMessages(
            threadId,
            "Bearer ${
                PreferenceHelper.getStringPreference(
                    this,
                    Constants.TOKEN
                ).toString()
            }"
        )
    }
}