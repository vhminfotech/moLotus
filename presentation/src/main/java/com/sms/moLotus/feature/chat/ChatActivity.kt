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
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.layout_header.*
import timber.log.Timber

class ChatActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    var messageList: ArrayList<Message> = ArrayList()
    private var chatAdapter: ChatAdapter? = null
    private var currentUserId: String = ""
    private var receiverUserId: String = ""
    var userName: String = ""
    var recipientsIds: ArrayList<String>? = ArrayList()
    var threadId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        currentUserId = intent?.getStringExtra("currentUserId").toString()
        receiverUserId = intent?.getStringExtra("receiverUserId").toString()
        threadId = intent?.getStringExtra("threadId").toString()
        userName = intent?.getStringExtra("userName").toString()
        Log.e("==========", "currentUserId:: $currentUserId")
        Log.e("==========", "recieverUserId:: $receiverUserId")
        txtTitle?.text = userName
        recipientsIds?.add(receiverUserId)
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        viewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
                MainViewModel::class.java
            )
        // getChatList()


        imgSend.setOnClickListener { //getMessage list empty then create thread else create message
            if (threadId.isEmpty()) {
                createThread()
            } else {
                createMessage(threadId)
            }
        }


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

    private fun createThread() {
        viewModel.createThread.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)
            /*val list : ArrayList<CreateThreadMutation.CreateThread> = it
            messageList = it*/

            Timber.e("createThread:: $it")

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
        recipientsIds?.let {
            viewModel.createThread(
                txtMessage.text.toString(), currentUserId,
                it, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
            )
        }
    }

    private fun createMessage(threadId: String) {
        viewModel.createMessage.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)
            /*val list : ArrayList<CreateThreadMutation.CreateThread> = it
            messageList = it*/
            Timber.e("createMessage:: $it")

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

        viewModel.createMessage(
            txtMessage.text.toString(), threadId,
            currentUserId, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
        )
    }

    private fun getChatList() {
        viewModel.allMessages.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)
            if (it.messages.isNotEmpty()) {
                messageList = it.messages
                initRecyclerView(messageList)
            }
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
            1,
            PreferenceHelper.getStringPreference(
                this,
                Constants.TOKEN
            ).toString()

        )
    }
}