package com.sms.moLotus.feature.chat

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetMessageListQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.QKApplication
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.ChatAdapter
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.RetrofitService
import com.sms.moLotus.viewmodel.ChatViewModel
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber


class ChatActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel

    lateinit var chatViewModel: ChatViewModel
    private val retrofitService = RetrofitService.getInstance()
    private var chatAdapter: ChatAdapter? = null
    private var receiverUserId: String = ""
    private var currentUserId: String = ""
    var userName: String = ""
    var recipientsIds: ArrayList<String>? = ArrayList()
    var threadId: String = ""
    var getMessageList: MutableList<GetMessageListQuery.Message> = mutableListOf()

    //    var chatMessageModel: ChatMessage? = null
    var chatMessageList: ArrayList<ChatMessage>? = ArrayList()
    private var mSocket: Socket? = null
    private var isConnected = true
    var myUserId = ""

    private val chatDatabase by lazy { ChatDatabase.getDatabase(this).getChatDao() }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
        mSocket?.off(Socket.EVENT_CONNECT, onConnect)
        /*mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)*/
        mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.off("getMessage", getMessage)
    }

    private val onConnect = Emitter.Listener {
        Log.i("====================", "connected")
        // mSocket?.emit("addUser", myUserId)
        if (!isConnected) {
            mSocket?.emit("addUser", myUserId)

            isConnected = true
        }

    }
    private val onDisconnect = Emitter.Listener {
        runOnUiThread {
            Log.i("====================", "disconnected")
            isConnected = false
            // mSocket?.emit("removeUser", myUserId)

        }
    }
    private val onConnectError = Emitter.Listener {
        runOnUiThread {
            Log.e("====================", "Error connecting")

        }
    }
    private val getMessage = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            Log.e("=============", "data:: $data")

            val senderId: String
            val text: String
            try {
                senderId = data.getString("senderId")
                text = data.getString("text")
            } catch (e: JSONException) {
                Log.e("====================", e.message.toString())
                return@Runnable
            }
            //removeTyping(username)

            Log.e("=============", "senderId:: $senderId")
            Log.e("=============", "message:: $text")
            addMessage(senderId, text)
        })
    }

    private fun addMessage(senderId: String, message: String) {
        Log.e("=============", "senderId:: $senderId")
        Log.e("=============", "message:: $message")
        // Log.e("=============", "chatMessage before :: ${chatMessageList?.size}")
        val iterator: MutableIterator<ChatMessage>? =
            chatMessageList?.iterator() as MutableIterator<ChatMessage>?
        var chatMessageModel: ChatMessage? = null
        while (iterator?.hasNext() == true) {
            val c: ChatMessage = iterator.next()
            chatMessageModel = ChatMessage(
                myUserId,
                senderId,
                c.threadId,
                message,
                c.dateSent,
                c.id
            )
        }
        Log.e("CHATMESSAGE", "chatMessageModel ::: $chatMessageModel")

        chatMessageModel?.let { chatMessageList?.add(it) }
//        chatMessageList?.size?.minus(1)?.let { chatAdapter?.notifyItemChanged(it) }
//        chatAdapter?.notifyDataSetChanged()
        //chatMessageList?.size?.minus(1)?.let { chatAdapter?.notifyItemInserted(it) }

        chatMessageList?.let { initRecyclerView(it) }

        Log.e("CHATMESSAGE", "chatMessageList after : ${chatMessageList?.size}")

    }

    override fun onBackPressed() {
        super.onBackPressed()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        myUserId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        val app = application as QKApplication
        mSocket = app.socket

        mSocket?.on(Socket.EVENT_CONNECT) {
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
            mSocket?.emit("addUser", currentUserId)
        }
        mSocket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.on("getMessage", getMessage)
        mSocket?.connect()

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

        chatViewModel =
            ViewModelProvider(this).get(ChatViewModel::class.java)

        getMessageList()

        imgSend.setOnClickListener {
            //getMessage list empty then create thread else create message
            Log.e("=====", "getMessageList.size:: ${getMessageList.size}")
            if (threadId.isEmpty() && getMessageList.size == 0) {
                Log.e("=====", "createThread")

                createThread(txtMessage.text.toString())
            } else {
                Log.e("=====", "createMessage : $threadId")

                createMessage(threadId, txtMessage.text.toString())
            }
        }


        Handler(Looper.getMainLooper()).postDelayed({
            // observeNotes()

        }, 10000)

    }

    private fun subscribeOnAddMessage() {
        /*viewModel.notifyNewMessageInsertedLiveData.observe(this, Observer {
            chatAdapter.notifyItemInserted(it)
        })*/
    }

    private fun initRecyclerView(list: List<ChatMessage>) {
        val layoutMgr = LinearLayoutManager(this)
        rvMessageList?.hasFixedSize()
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        chatAdapter = ChatAdapter(list.toMutableList(), this)
        rvMessageList.adapter = chatAdapter
    }

    private fun createThread(message: String) {
        viewModel.createThread.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)
            /*val list : ArrayList<CreateThreadMutation.CreateThread> = it
            messageList = it*/
            Timber.e("createThread:: $it")
            getMessageList()
            mSocket?.emit(
                "sendMessage", currentUserId,
                recipientsIds?.get(0).toString(),
                it.createThread?.message.toString()
            )

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
                message, currentUserId,
                it, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
            )
        }
    }

    private fun createMessage(threadId: String, message: String) {
        viewModel.createMessage.observe(this, {
            Log.e("=====", "response:: $it")
            // initRecyclerView(it)
            /*val list : ArrayList<CreateThreadMutation.CreateThread> = it
            messageList = it*/
            Timber.e("createMessage:: $it")
            getMessageList()
            mSocket?.emit(
                "sendMessage", currentUserId,
                recipientsIds?.get(0).toString(),
                it.createMessage?.message.toString()
            )

        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
            val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
            message,
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
        )
    }


    private fun observeNotes() {
        lifecycleScope.launch {
            chatDatabase.getAllChat(
                PreferenceHelper.getStringPreference(
                    this@ChatActivity,
                    Constants.USERID
                ).toString()
            ).observe(this@ChatActivity, { list ->
                Log.e("=============", "list:: $list")
                // chatMessageList = list as MutableList<ChatMessage>?
                // chatMessageList?.let { initRecyclerView(it) }

                /*if (chatMessageList.isNotEmpty()) {
                    adapter.submitList(notesList)
                }*/
            })

        }
    }


    private fun getMessageList() {
        viewModel.allMessages.observe(this, {
            Log.e("=====", "response:: $it")

            if (it.getMessageList?.messages?.isNotEmpty() == true) {
                getMessageList =
                    it.getMessageList.messages as MutableList<GetMessageListQuery.Message>
                var chatMessageModel: ChatMessage? = null
                getMessageList.forEachIndexed { index, message ->
                    chatMessageModel = ChatMessage(
                        myUserId,
                        getMessageList[index].id.toString(),
                        getMessageList[index].senderId.toString(),
                        getMessageList[index].threadId.toString(),
                        getMessageList[index].message.toString(),
                        getMessageList[index].dateSent.toString(),
                    )

                    Log.e("===================", "Chat Message Model: $chatMessageModel")


//                    lifecycleScope.launch {
//                    }

                    chatMessageList?.add(chatMessageModel!!)

                    chatViewModel.insert(chatMessageModel!!)
                   // chatViewModel.insertAllMessages(chatMessageList!!)

                }
                Log.e("===================", "chatMessageList::: $$chatMessageList")


                //Log.e("CHATMESSAGE", "list : $chatMessageList")

                threadId = if (threadId.isEmpty()) {
                    getMessageList[0].threadId.toString()
                } else {
                    threadId
                }

                txtMessage.text = null

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