package com.sms.moLotus.feature.chat.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.file_picker.FileType
import com.github.file_picker.showFilePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetGroupMessageListQuery
import com.sms.moLotus.GetMessageListQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.QKApplication
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.LogHelper
import com.sms.moLotus.feature.chat.adapter.ChatAdapter
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.viewmodel.ChatViewModel
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_attachment_preview.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.imgSend
import kotlinx.android.synthetic.main.dialog_delete_message.*
import kotlinx.android.synthetic.main.dialog_delete_message.btnCancel
import kotlinx.android.synthetic.main.dialog_delete_message.txtDesc
import kotlinx.android.synthetic.main.dialog_send_attachments.*
import kotlinx.android.synthetic.main.layout_attachments.view.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.M)
class ChatActivity : AppCompatActivity(), OnMessageClickListener {
    lateinit var viewModel: MainViewModel
    private lateinit var chatViewModel: ChatViewModel
    private var chatAdapter: ChatAdapter? = null
    private var currentUserId: String = ""
    var userName: String = ""
    var groupName: String = ""
    private var recipientsIds: ArrayList<String>? = ArrayList()
    var threadId: String = ""
    private var getMessageList: MutableList<GetMessageListQuery.Message> = mutableListOf()
    private var getGroupMessageList: MutableList<GetGroupMessageListQuery.Message> = mutableListOf()
    private val messageIdList: ArrayList<String> = ArrayList()
    var linearLayout: LinearLayout? = null
    private var pos = 0
    private var chatMessageList: ArrayList<ChatMessage>? = ArrayList()
    private var mSocket: Socket? = null
    private var isConnected = true
    private var myUserId = ""
    private var myUserName = ""
    private var flag: Boolean? = true
    var isGroup: Boolean = false
    private val chatDatabase by lazy { ChatDatabase.getDatabase(this).getChatDao() }
    var delay: Long = 1000 // 1 seconds after user stops typing
    var last_text_edit: Long = 0
    var handler = Handler(Looper.getMainLooper())

    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            //  mSocket?.emit("typing", false)
            LogHelper.e("=============", "input_finish_checker")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        toolbar?.title = "Delete Message"
        myUserId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        myUserName = PreferenceHelper.getStringPreference(this, Constants.USERNAME).toString()
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
        // mSocket?.off("typing")?.on("typing", typing)
        mSocket?.connect()

        currentUserId = intent?.getStringExtra("currentUserId").toString()
        //receiverUserId = intent?.getStringExtra("receiverUserId").toString()
        recipientsIds = intent?.getStringArrayListExtra("receiverUserId")
        threadId = intent?.getStringExtra("threadId").toString()
        flag = intent?.getBooleanExtra("flag", false)
        isGroup = intent.getBooleanExtra("isGroup", false)
        userName = intent?.getStringExtra("userName").toString()
        groupName = intent?.getStringExtra("groupName").toString()
        LogHelper.e("NewGroupActivity", "userName:: $userName")

        if (isGroup || userName.isEmpty() || userName == "" || userName == "null") {
            txtTitle?.text = groupName
        } else {
            txtTitle?.text = userName
        }
//        LogHelper.e("NewGroupActivity","receiverUserId:: $receiverUserId")

        //recipientsIds?.add(receiverUserId)


        LogHelper.e("NewGroupActivity", "recipientsIds:: $recipientsIds")
        LogHelper.e("NewGroupActivity", "isGroup:: $isGroup")


        imgBack?.setOnClickListener {
            onBackPressed()
        }
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        runOnUiThread {
            //chatViewModel.deleteTable()
        }

        if (!isGroup) {
            getMessageList(threadId)
        } else {
            getGroupMessageList(threadId)
        }

        imgSend.setOnClickListener {
            //getMessage list empty then create thread else create message
            if (flag == true) {
                createThread(txtMessage.text.toString(), isGroup, groupName)
            } else {
                if ((threadId.isEmpty() || threadId == "null")) {
                    Log.e("=====", "createThread")
                    createThread(txtMessage.text.toString(), isGroup, groupName)
                } else {
                    Log.e("=====", "createMessage : $threadId")
                    createMessage(threadId, txtMessage.text.toString())
                }
            }
        }



        txtMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handler.removeCallbacks(input_finish_checker)
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length!! > 0) {

                    //mSocket?.emit("typing", true)
                    last_text_edit = System.currentTimeMillis()
                    handler.postDelayed(input_finish_checker, delay)
                    LogHelper.e("=============", "afterTextChanged")

                }
            }

        })


        llName.setOnClickListener {
            /* if (isGroup) {
                 val intent = Intent(this, GroupDetailsActivity::class.java)
                 startActivity(intent)
                 overridePendingTransition(0, 0)
             }*/
        }

        imgOpenGallery.setOnClickListener {
            showAttachmentOptions()

        }


    }

    private fun showAttachmentOptions() {

        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.layout_attachments, null)


        view.txtCamera.setOnClickListener {
            dialog.dismiss()
        }

        view.txtAddPhoto.setOnClickListener {
            showFilePicker(FileType.IMAGE)
            dialog.dismiss()
        }

        view.txtAddVideo.setOnClickListener {
            showFilePicker(FileType.VIDEO)
            dialog.dismiss()
        }

        view.txtAddDocuments.setOnClickListener {
            openFolder()
            dialog.dismiss()
        }


        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()
    }

    private fun openFolder() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Complete action using"),
            100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val data = data?.data
            Log.e("============", "data:::: $data")
            showSendAttachmentDialog()

        }
    }

    private fun showSendAttachmentDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_send_attachments)

        dialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSend.setOnClickListener {

            dialog.dismiss()
        }
        dialog.show()

    }


    private fun showFilePicker(fileType: FileType) {
        showFilePicker(
            gridSpanCount = 3,
            fileType = fileType,
            limitItemSelection = 1,
            submitText = "Next",
            accentColor = ContextCompat.getColor(this, R.color.tools_theme),
            titleTextColor = ContextCompat.getColor(this, R.color.black),
            submitTextColor = ContextCompat.getColor(this, R.color.white),
        ) {
            LogHelper.e("===============", "media : ${it[0].file}")

            val intent = Intent(this, AttachmentPreviewActivity::class.java)
                .putExtra("fileName", it[0].file.toString())
                .putExtra("fileType", fileType.name)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("*/*")
        startActivityForResult(
            Intent.createChooser(intent, null),
            100
        )
    }

    private fun initRecyclerView(list: List<ChatMessage>) {
        val layoutMgr = LinearLayoutManager(this)
        rvMessageList?.hasFixedSize()
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        chatAdapter = ChatAdapter(list.toMutableList(), this, this)
        rvMessageList.adapter = chatAdapter
    }

    private fun createThread(message: String, isGroup: Boolean, groupName: String) {

        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, myUserName
        )


        addMessage(currentUserId, message, "", "")
        viewModel.createThread.observe(this, {
            LogHelper.e("======================", "createThread:: ${it.createThread?.id}")
            LogHelper.e("======================", "isGroup:: $isGroup")
            txtMessage.text = null
            if (!isGroup) {
                getMessageList(it.createThread?.id.toString())
            } else {
                getGroupMessageList(it.createThread?.id.toString())
            }
        })
        viewModel.errorMessage.observe(this, {
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
        })
        recipientsIds?.let {
            viewModel.createThread(
                message, currentUserId,
                it, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString(),
                isGroup, groupName
            )
        }


    }

    private fun createMessage(threadId: String, message: String) {
        Timber.e("recipientsIds:: " + recipientsIds)
        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, myUserName
        )
        addMessage(currentUserId, message, "", "")

        viewModel.createMessage.observe(this, {
            Timber.e("createMessage:: $it")
            txtMessage.text = null
        })
        viewModel.errorMessage.observe(this, {
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
                chatMessageList = list as ArrayList<ChatMessage>?
                initRecyclerView(list.reversed())
            })

        }
    }

    private fun getMessageList(threadId: String) {
        Log.e("=====", "getMessageList threadId : $threadId")

        viewModel.allMessages.observe(this, {
            LogHelper.e("======================", "getMessageList:: $it")

            if (it.getMessageList?.messages?.isNotEmpty() == true) {
                getMessageList =
                    it.getMessageList.messages as MutableList<GetMessageListQuery.Message>
                var chatMessageModel: ChatMessage? = null
                getMessageList.forEachIndexed { index, _ ->
                    chatMessageModel = ChatMessage(
                        myUserId,
                        getMessageList[index].id.toString(),
                        getMessageList[index].senderId.toString(),
                        getMessageList[index].threadId.toString(),
                        getMessageList[index].message.toString(),
                        getMessageList[index].dateSent.toString(),
                        ""
                    )

                    // chatMessageList?.add(chatMessageModel!!)
                    chatViewModel.insert(chatMessageModel!!)
                    // chatViewModel.insertAllMessages(chatMessageList!!)

                }
                //Log.e("===================", "chatMessageList::: $$chatMessageList")

                this.threadId = if (threadId.isEmpty() || threadId == "null") {
                    getMessageList[0].threadId.toString()
                } else {
                    threadId
                }

                txtMessage.text = null

                LogHelper.e("==================", "thread id create message: $threadId")
                Handler(Looper.getMainLooper()).postDelayed({
                    observeMessages(threadId)
                }, 500)

            }
        })
        viewModel.errorMessage.observe(this, {
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
            recipientsIds?.get(0).toString(),
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()

        )
    }

    private fun getGroupMessageList(threadId: String) {
        Log.e("=====", "getGroupMessageList threadId : $threadId")
        viewModel.allGroupMessages.observe(this, {
            LogHelper.e("======================", "getGroupMessageList:: $it")

            if (it.getGroupMessageList?.messages?.isNotEmpty() == true) {
                getGroupMessageList =
                    it.getGroupMessageList.messages as MutableList<GetGroupMessageListQuery.Message>
                var chatMessageModel: ChatMessage? = null
                getGroupMessageList.forEachIndexed { index, _ ->
                    chatMessageModel = ChatMessage(
                        myUserId,
                        getGroupMessageList[index].id.toString(),
                        getGroupMessageList[index].senderId.toString(),
                        getGroupMessageList[index].threadId.toString(),
                        getGroupMessageList[index].message.toString(),
                        getGroupMessageList[index].dateSent.toString(),
                        getGroupMessageList[index].userName.toString(),
                    )

                    // chatMessageList?.add(chatMessageModel!!)
                    chatViewModel.insert(chatMessageModel!!)
                    // chatViewModel.insertAllMessages(chatMessageList!!)

                }
                this.threadId = if (threadId.isEmpty() || threadId == "null") {
                    getGroupMessageList[0].threadId.toString()
                } else {
                    threadId
                }
                txtMessage.text = null
                LogHelper.e("==================", "thread id create message: $threadId")
                Handler(Looper.getMainLooper()).postDelayed({
                    observeMessages(threadId)
                }, 500)

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
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })

        viewModel.getAllGroupMessages(
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()

        )
    }

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
                linearLayout?.setBackgroundColor(
                    resources.getColor(
                        android.R.color.transparent,
                        theme
                    )
                )

                return true
            }

            R.id.delete -> {
                toolbar?.visibility = View.GONE
                linearLayout?.let { showDeleteDialog(it, messageIdList, pos) }
            }
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteDialog(
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
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })
        viewModel.deleteMessage(threadId, myUserId, messageIdList)
    }


    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
        mSocket?.off(Socket.EVENT_CONNECT, onConnect)
        /*mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)*/
        mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.off("getMessage", getMessage)
        //  mSocket?.off("typing", typing)
    }

    private val onConnect = Emitter.Listener {
        Log.i("====================", "connected")
        if (!isConnected) {
            mSocket?.emit("addUser", myUserId)
            isConnected = true
        }

    }
    private val onDisconnect = Emitter.Listener {
        runOnUiThread {
            isConnected = false
            // mSocket?.emit("removeUser", myUserId)

        }
    }
    private val onConnectError = Emitter.Listener {
        runOnUiThread {
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
            val senderId: String
            val text: String
            val currTime: String
            val name: String
            try {
                senderId = data.getString("senderId")
                text = data.getString("text")
                name = data.getString("name")
                currTime = data.getString("currTime")
            } catch (e: JSONException) {
                return@Runnable
            }
            //removeTyping(username)
            txtTyping?.text = ""
            txtTyping?.visibility = View.GONE
            if (isGroup) {
                addMessage(senderId, text, currTime, name)
            } else {
                addMessage(senderId, text, currTime, "")
            }
        })
    }

    private fun addMessage(senderId: String, message: String, currTime: String, userName: String) {
        val dateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
        val iterator: MutableIterator<ChatMessage>? = chatMessageList?.iterator()
        var chatMessageModel: ChatMessage? = null

        while (iterator?.hasNext() == true) {
            val c: ChatMessage = iterator.next()
            val time = if (currTime.isEmpty()) {
                dateFormat
            } else {
                currTime
            }
            chatMessageModel = ChatMessage(
                myUserId,
                c.id,
                senderId,
                c.threadId,
                message,
                dateFormat,
                userName
            )
        }

        //chatAdapter?.updateList(chatMessageModel)
        chatMessageModel?.let { chatMessageList?.add(it) }
        chatMessageList?.let { chatAdapter?.updateList(it as MutableList<ChatMessage>) }
        rvMessageList?.scrollToPosition(chatMessageList?.size!!.toInt() - 1)
    }


}