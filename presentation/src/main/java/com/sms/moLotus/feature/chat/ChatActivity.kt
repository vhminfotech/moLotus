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
import com.sms.moLotus.GetMessageListQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.ChatAdapter
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.layout_header.*
import timber.log.Timber

class ChatActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    private var chatAdapter: ChatAdapter? = null
    private var receiverUserId: String = ""
    private var currentUserId: String = ""
    var userName: String = ""
    var recipientsIds: ArrayList<String>? = ArrayList()
    var threadId: String = ""
    var getMessageList: MutableList<GetMessageListQuery.Message> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        currentUserId = intent?.getStringExtra("currentUserId").toString()
        receiverUserId = intent?.getStringExtra("receiverUserId").toString()
        threadId = intent?.getStringExtra("threadId").toString()
        userName = intent?.getStringExtra("userName").toString()
        txtTitle?.text = userName
        recipientsIds?.add(receiverUserId)
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        viewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
                MainViewModel::class.java
            )
        //

        getMessageList()

        imgSend.setOnClickListener {
            //getMessage list empty then create thread else create message
            Log.e("=====", "getMessageList.size:: ${getMessageList.size}")
            if (threadId.isEmpty() && getMessageList.size == 0) {
                Log.e("=====", "createThread")

                createThread()
            } else {
                Log.e("=====", "createMessage : $threadId")

                createMessage(threadId)
            }
        }


    }

    private fun subscribeOnAddMessage() {
        /*viewModel.notifyNewMessageInsertedLiveData.observe(this, Observer {
            chatAdapter.notifyItemInserted(it)
        })*/
    }

    private fun initRecyclerView(list: MutableList<GetMessageListQuery.Message>) {
        val layoutMgr = LinearLayoutManager(this)
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        chatAdapter = ChatAdapter(list.toMutableList(), this)
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
            getMessageList()

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
            getMessageList()
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
            txtMessage.text.toString(),
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
        )
    }

    private fun getMessageList() {
        viewModel.allMessages.observe(this, {
            Log.e("=====", "response:: $it")
            txtMessage.setText("")
            if (it.getMessageList?.messages?.isNotEmpty() == true) {
                getMessageList =
                    it.getMessageList.messages as MutableList<GetMessageListQuery.Message>
                threadId = if (threadId.isNullOrEmpty()) {
                    getMessageList[0].threadId.toString()
                } else {
                    threadId
                }
                initRecyclerView(getMessageList)
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
            threadId,
            receiverUserId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()

        )
    }
}