package com.sms.moLotus.feature.chat

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.RetrofitService
import com.sms.moLotus.viewmodel.ChatViewModel
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.dialog_delete_message.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.M)
class ChatActivity : AppCompatActivity(), OnMessageClickListener {
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
    var delay: Long = 1000 // 1 seconds after user stops typing

    var last_text_edit: Long = 0
    var handler = Handler(Looper.getMainLooper())

    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            mSocket?.emit("typing", false)
            LogHelper.e("=============", "input_finish_checker")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
        mSocket?.off(Socket.EVENT_CONNECT, onConnect)
        /*mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)*/
        mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.off("getMessage", getMessage)
        mSocket?.off("typing", typing)
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

    private val typing = Emitter.Listener { data ->
        runOnUiThread {
            LogHelper.e("==============", "data:: $data")
            val userTyping = data[0] as JSONObject
            txtTyping?.text = userTyping.getString("data").toString()

            if (userTyping.getString("data").toString().isNotEmpty()) {
                txtTyping?.visibility = View.VISIBLE
            } else {
                txtTyping?.visibility = View.GONE
            }

        }
    }

    private val getMessage = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            Log.e("=============", "data:: $data")

            val senderId: String
            val text: String
            val currTime: String
            try {
                senderId = data.getString("senderId")
                text = data.getString("text")
                currTime = data.getString("currTime")
            } catch (e: JSONException) {
                Log.e("====================", e.message.toString())
                return@Runnable
            }
            //removeTyping(username)
            txtTyping?.text = ""
            txtTyping?.visibility = View.GONE

            addMessage(senderId, text, currTime)
        })
    }

    private fun addMessage(senderId: String, message: String, currTime: String) {
        Log.e("=============", "senderId:: $senderId")
        Log.e("=============", "message:: $message")
        val iterator: MutableIterator<ChatMessage>? =
            chatMessageList?.iterator()
        var chatMessageModel: ChatMessage? = null

        while (iterator?.hasNext() == true) {
            val c: ChatMessage = iterator.next()
            val time = if (currTime.isEmpty()) {
                c.dateSent
            } else {
                currTime
            }
            chatMessageModel = ChatMessage(
                myUserId,
                c.id,
                senderId,
                c.threadId,
                message,
                time,
            )
        }
        Log.e("CHATMESSAGE", "chatMessageModel ::: $chatMessageModel")

        chatMessageModel?.let { chatMessageList?.add(it) }
        chatMessageList?.let { chatAdapter?.updateList(it) }
        rvMessageList?.scrollToPosition(chatMessageList?.size!!.toInt() - 1)

        Log.e("CHATMESSAGE", "chatMessageList after : ${chatMessageList?.size}")
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        toolbar?.title = "Delete Message"
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
        mSocket?.off("getMessage")?.on("getMessage", getMessage)
        mSocket?.off("typing")?.on("typing", typing)
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
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        runOnUiThread {
            //chatViewModel.deleteTable()
        }

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
            observeMessages(threadId)
        }, 500)

        txtMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handler.removeCallbacks(input_finish_checker);
                /* if (p0?.length!! > 0) {
                     mSocket?.off("typing")?.on("typing", typing)
                     mSocket?.emit("typing")
                 }else{
                     mSocket?.off("typing")
                 }*/
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length!! > 0) {

                    mSocket?.emit("typing", true)
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                    LogHelper.e("=============", "afterTextChanged")


                } else {

                }
            }

        })

    }

    private fun initRecyclerView(list: List<ChatMessage>) {
        val layoutMgr = LinearLayoutManager(this)
        rvMessageList?.hasFixedSize()
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        chatAdapter = ChatAdapter(list.toMutableList(), this, this)
        rvMessageList.adapter = chatAdapter
    }

    private fun createThread(message: String) {
        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds?.get(0).toString(),
            message
        )
        //addMessage(currentUserId, message, "")
        viewModel.createThread.observe(this, {
            Timber.e("createThread:: $it")
            txtMessage.text = null
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
                message, currentUserId,
                it, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
            )
        }

    }

    private fun createMessage(threadId: String, message: String) {
        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds?.get(0).toString(),
            message
        )
        addMessage(currentUserId, message, "")

        viewModel.createMessage.observe(this, {
            Timber.e("createMessage:: $it")
            txtMessage.text = null
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

    private fun observeMessages(threadId: String) {
        lifecycleScope.launch {
            chatDatabase.getAllChat(
                threadId
            ).observe(this@ChatActivity, { list ->
                Log.e("allMessages", "chatMessageList:: $list")
                Log.e("allMessages", "chatMessageList length:: ${chatMessageList?.size}")
                chatMessageList = list as ArrayList<ChatMessage>?
                initRecyclerView(list.reversed())
            })

        }
    }


    private fun getMessageList() {
        viewModel.allMessages.observe(this, {
            Log.e("allMessages", "allMessages:: $it")
            Log.e("allMessages", "allMessages lengtj::: ${it.getMessageList?.messages?.size}")

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

                    // chatMessageList?.add(chatMessageModel!!)
                    chatViewModel.insert(chatMessageModel!!)
                    // chatViewModel.insertAllMessages(chatMessageList!!)

                }
                //Log.e("===================", "chatMessageList::: $$chatMessageList")

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

    private val messageIdList: ArrayList<String> = ArrayList()
    var linearLayout: LinearLayout? = null
    var pos = 0

    override fun onMessageClick(item: ChatMessage?, llOnClick: LinearLayout, adapterPosition: Int) {
        LogHelper.e("onMessageClick", "===== itemclicked : $item")
        messageIdList.add(item?.id.toString())
        toolbar?.visibility = View.VISIBLE
        LogHelper.e("MESSAGEIDLIST", "==== $messageIdList")
        linearLayout = llOnClick
        pos = adapterPosition
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toolbar?.visibility = View.GONE
                linearLayout?.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))

                return true
            }

            R.id.delete -> {
                toolbar?.visibility = View.GONE
                linearLayout?.let { showDialog(it, messageIdList, pos) }
            }
        }
        return true
    }

    private fun showDialog(
        llOnClick: LinearLayout,
        messageIdList: ArrayList<String>,
        adapterPosition: Int
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_message)

        dialog.txtDesc?.text = "Do you want to delete the message?"

        dialog.btnCancel.setOnClickListener {
            llOnClick.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            dialog.dismiss()
        }
        dialog.btnDelete.setOnClickListener {
            llOnClick.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            chatAdapter?.deleteMessage(adapterPosition)
            deleteMessage(messageIdList)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun deleteMessage(messageIdList: ArrayList<String>) {
        viewModel.deleteMessage.observe(this, {
            runOnUiThread {
                chatViewModel.deleteMessage(messageIdList)
                toast("Message Deleted!")
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
        viewModel.deleteMessage(threadId, myUserId, messageIdList)
    }
}