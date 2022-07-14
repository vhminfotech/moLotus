package com.sms.moLotus.feature.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appexecutors.picker.Picker
import com.appexecutors.picker.Picker.Companion.PICKED_MEDIA_LIST
import com.appexecutors.picker.Picker.Companion.REQUEST_CODE_PICKER
import com.appexecutors.picker.utils.PickerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetGroupMessageListQuery
import com.sms.moLotus.GetMessageListQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.QKApplication
import com.sms.moLotus.customview.CustomProgressDialog
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.Utils
import com.sms.moLotus.feature.chat.LogHelper
import com.sms.moLotus.feature.chat.adapter.ChatAdapter
import com.sms.moLotus.feature.chat.adapter.ChatContactListAdapter
import com.sms.moLotus.feature.chat.listener.OnChatContactClickListener
import com.sms.moLotus.feature.chat.listener.OnMessageClickListener
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.chat.model.Users
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.viewmodel.ChatViewModel
import ezvcard.Ezvcard
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.dialog_delete_message.*
import kotlinx.android.synthetic.main.dialog_select_contacts.view.*
import kotlinx.android.synthetic.main.layout_attachments.view.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.M)
class ChatActivity : AppCompatActivity(), OnMessageClickListener, OnChatContactClickListener {
    lateinit var viewModel: MainViewModel
    private lateinit var chatViewModel: ChatViewModel
    private var chatAdapter: ChatAdapter? = null
    private var currentUserId: String? = ""
    var userName: String? = ""
    var groupName: String? = ""
    private var recipientsIds: ArrayList<String>? = ArrayList()
    var threadId: String? = ""
    private var getMessageList: MutableList<GetMessageListQuery.Message> = mutableListOf()
    private var getGroupMessageList: MutableList<GetGroupMessageListQuery.Message> = mutableListOf()
    private val messageIdList: ArrayList<String> = ArrayList()
    var linearLayout: LinearLayout? = null
    private var pos = 0
    private var chatMessageList: ArrayList<ChatMessage>? = ArrayList()
    var usersList: ArrayList<Users> = ArrayList()

    companion object {
        var mSocket: Socket? = null
        var myUserName: String? = ""

    }

    private var isConnected = true
    private var myUserId: String? = ""
    private var flag: Boolean? = true
    var isGroup: Boolean = false
    private var isNotParticipant: Boolean = false
    private val chatDatabase by lazy { ChatDatabase.getDatabase(this).getChatDao() }

    //var delay: Long = 1000 // 1 seconds after user stops typing
    var last_text_edit: Long = 0

    //var handler = Handler(Looper.getMainLooper())
    private var attachmentList = ArrayList<String>()
    private var attachmentUrl: String? = null
    private var shareText: String? = null
    private var customProgressDialog: CustomProgressDialog? = null
    var selectedUsersList: ArrayList<Users?> = ArrayList()
    /*private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            //  mSocket?.emit("typing", false)
        }
    }*/

    override fun onResume() {
        super.onResume()
        customProgressDialog = CustomProgressDialog(this)
        myUserId = PreferenceHelper.getStringPreference(this, Constants.USERID)
        myUserName = PreferenceHelper.getStringPreference(this, Constants.USERNAME)
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

        currentUserId = intent?.getStringExtra("currentUserId")
        //receiverUserId = intent?.getStringExtra("receiverUserId").toString()
        recipientsIds = intent?.getStringArrayListExtra("receiverUserId")
        threadId = intent?.getStringExtra("threadId")
        flag = intent?.getBooleanExtra("flag", false)
        isGroup = intent.getBooleanExtra("isGroup", false)
        isNotParticipant = intent.getBooleanExtra("isNotParticipant", false)
        userName = intent.getStringExtra("userName")
        groupName = intent.getStringExtra("groupName")
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        //LogHelper.e("NewGroupActivity", "userName:: $userName")

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        if (isGroup || userName?.isEmpty() == true || userName == "" || userName == "null") {
            txtTitle?.text = groupName
        } else {
            txtTitle?.text = userName
        }

        if (isNotParticipant) {
            txtNoLongerParticipant?.visibility = View.VISIBLE
            imgSend?.visibility = View.GONE
            imgOpenGallery?.visibility = View.GONE
            txtMessage?.visibility = View.GONE
        } else {
            imgSend?.visibility = View.VISIBLE
            imgOpenGallery?.visibility = View.VISIBLE
            txtMessage?.visibility = View.VISIBLE
            txtNoLongerParticipant?.visibility = View.GONE
        }

        runOnUiThread {
            //chatViewModel.deleteTable()
        }
        LogHelper.e("CHATACTIVITY", "=== Onresume")

        LogHelper.e("CHATACTIVITY", "===threadId:: $threadId")
        LogHelper.e("CHATACTIVITY", "===isGroup:: $isGroup")
        if (!isGroup) {
            getMessageList(threadId.toString())
        } else {
            getGroupMessageList(threadId.toString())
        }

        observeUsers()

        setListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        LogHelper.e("CHATACTIVITY", "=== onCreate threadId:: $threadId")
        LogHelper.e("CHATACTIVITY", "=== onCreate isGroup:: $isGroup")
    }


    private fun setListeners() {
        imgBack?.setOnClickListener {
            onBackPressed()
        }

        imgSend.setOnClickListener {
            //getMessage list empty then create thread else create message
            if (flag == true) {
                createThread(txtMessage.text.toString(), isGroup, groupName.toString(), "")
            } else {
                if ((threadId?.isEmpty() == true || threadId == "null")) {
                    createThread(
                        txtMessage.text.toString(), isGroup,
                        groupName.toString(), ""
                    )
                } else {
                    if (isGroup) {
                        createMessage(
                            threadId.toString(),
                            txtMessage.text.toString(),
                            "",
                            ""
                        )
                    } else {
                        createMessage(
                            threadId.toString(),
                            txtMessage.text.toString(),
                            recipientsIds?.get(0).toString(),
                            ""
                        )
                    }
                }
            }
        }

        txtMessage?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //handler.removeCallbacks(input_finish_checker)
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length!! > 0) {
                    //mSocket?.emit("typing", true)
                    last_text_edit = System.currentTimeMillis()
                    //handler.postDelayed(input_finish_checker, delay)
                }
            }
        })

        llName.setOnClickListener {
            if (isGroup) {
                val intent = Intent(this, GroupDetailsActivity::class.java)
                    .putExtra("groupId", threadId)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }

        imgOpenGallery.setOnClickListener {
            showAttachmentOptions()
        }
    }

    private fun showAttachmentOptions() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.layout_attachments, null)
        val mPickerOptions =
            PickerOptions.init().apply {
                maxCount = 1                        //maximum number of images/videos to be picked
                maxVideoDuration = 10               //maximum duration for video capture in seconds
                allowFrontCamera = true             //allow front camera use
                excludeVideos = false               //exclude or include video functionalities
            }

        view.txtCamera.setOnClickListener {
            Picker.startPicker(this, mPickerOptions)
            dialog.dismiss()
        }

        view.txtAddPhoto.setOnClickListener {
            //showFilePicker(FileType.IMAGE)
            dialog.dismiss()
        }

        view.txtAddVideo.setOnClickListener {
            //showFilePicker(FileType.VIDEO)
            dialog.dismiss()
        }

        view.txtAddDocuments.setOnClickListener {
            openFolder()
            dialog.dismiss()
        }

        view.txtAddContacts.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
                .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

            startActivityForResult(
                Intent.createChooser(intent, null),
                101
            )
            dialog.dismiss()
        }

        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()
    }


    private fun openFolder() {
        val intent = Intent()
        intent.type = "application/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Complete action using"),
            100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val document = data?.data
//            showSendAttachmentDialog()
            val intent = Intent(this, AttachmentPreviewActivity::class.java)
                .putExtra("fileName", document?.toString())
                //.putExtra("fileType", fileType.name)
                .putExtra("threadId", threadId)
                .putExtra("isGroup", isGroup)
                .putExtra("groupName", groupName)
                .putExtra("flag", flag)
                .putExtra("recipientsIds", recipientsIds)
                .putExtra("currentUserId", currentUserId)
            startActivity(intent)
            overridePendingTransition(0, 0)
        } else if (requestCode == 101 && resultCode == RESULT_OK) {
            val contact = data?.data
            val map = getDataFromContacts(Uri.parse(contact.toString()))
            val name = map["name"]
            val number = map["number"]
            //txtMessage?.setText("<name: $name>\n<number: $number>")

            val intent = Intent(this, SendContactActivity::class.java)
                .putExtra("name", name.toString())
                .putExtra("number", number.toString())
                .putExtra("vCard", getVCard(Uri.parse(contact.toString())))
                .putExtra("threadId", threadId)
                .putExtra("isGroup", isGroup)
                .putExtra("groupName", groupName)
                .putExtra("flag", flag)
                .putExtra("recipientsIds", recipientsIds)
                .putExtra("currentUserId", currentUserId)
            startActivity(intent)
            overridePendingTransition(0, 0)

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICKER) {
            val mImageList =
                data?.getStringArrayListExtra(PICKED_MEDIA_LIST) as ArrayList //List of selected/captured images/videos
            mImageList.map {
                val intent = Intent(this, AttachmentPreviewActivity::class.java)
                    .putExtra("fileName", it.toString())
                    //.putExtra("fileType", fileType.name)
                    .putExtra("threadId", threadId)
                    .putExtra("isGroup", isGroup)
                    .putExtra("groupName", groupName)
                    .putExtra("flag", flag)
                    .putExtra("recipientsIds", recipientsIds)
                    .putExtra("currentUserId", currentUserId)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun getDataFromContacts(contactData: Uri): Map<String, String> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        val cursor: Cursor? = applicationContext.contentResolver.query(
            contactData, projection,
            null, null, null
        )
        cursor?.moveToFirst()

        val numberColumnIndex: Int? =
            cursor?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val number: String? = numberColumnIndex?.let { cursor.getString(it) }

        val nameColumnIndex: Int? =
            cursor?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val name: String? = nameColumnIndex?.let { cursor.getString(it) }
        val map = HashMap<String, String>()
        map["name"] = name.toString()
        map["number"] = number.toString()
        cursor?.close()
        return map
    }

    private fun getVCard(contactData: Uri): String? {
        val lookupKey =
            contentResolver.query(contactData, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
            }

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        return contentResolver.openAssetFileDescriptor(vCardUri, "r")
            ?.createInputStream()
            ?.readBytes()
            ?.let { bytes -> String(bytes) }
    }

    /*private fun showSendAttachmentDialog() {
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
    }*/

    /*private fun showFilePicker(fileType: FileType) {
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
            .putExtra("threadId", threadId)
            .putExtra("isGroup", isGroup)
            .putExtra("groupName", groupName)
            .putExtra("flag", flag)
            .putExtra("recipientsIds", recipientsIds)
            .putExtra("currentUserId", currentUserId)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
    }*/

    private fun initRecyclerView(list: List<ChatMessage>) {
        val layoutMgr = LinearLayoutManager(this)
        rvMessageList?.hasFixedSize()
        layoutMgr.stackFromEnd = true
        rvMessageList.layoutManager = layoutMgr
        chatAdapter = ChatAdapter(list.toMutableList(), this, this)
        rvMessageList.adapter = chatAdapter
    }

    private fun createThread(message: String, isGroup: Boolean, groupName: String, url: String) {
        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, myUserName, url
        )
        addMessage(currentUserId.toString(), message, "", "", url)
        viewModel.createThread.observe(this) {
            txtMessage.text = null
            if (!isGroup) {
                getMessageList(it.createThread?.id.toString())
            } else {
                getGroupMessageList(it.createThread?.id.toString())
            }
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

        viewModel.createThread(
            message, currentUserId.toString(),
            recipientsIds, PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString(),
            isGroup, groupName, url
        )
    }

    private fun createMessage(threadId: String, message: String, receiverId: String, url: String) {
        mSocket?.emit(
            "sendMessage", currentUserId,
            recipientsIds,
            message, myUserName, url
        )
        addMessage(currentUserId.toString(), message, "", "", url)

        viewModel.createMessage.observe(this) {
            txtMessage.text = null
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

    private fun observeMessages(threadId: String) {
        lifecycleScope.launch {
            chatDatabase.getAllChat(
                threadId
            ).observe(this@ChatActivity) { list ->
                chatMessageList = list as ArrayList<ChatMessage>?
                initRecyclerView(list.reversed())
            }

        }
    }

    private fun getMessageList(threadId: String) {
        viewModel.allMessages.observe(this) {
            LogHelper.e("CHATACTIVITY","list :: ${it.getMessageList}")
            if (it.getMessageList?.messages?.isNotEmpty() == true) {
                getMessageList =
                    it.getMessageList.messages as MutableList<GetMessageListQuery.Message>
                var chatMessageModel: ChatMessage? = null
                getMessageList.forEachIndexed { index, _ ->
                    val fileName: String =
                        if (getMessageList[index].url?.endsWith(".mp4") == true || getMessageList[index].url?.endsWith(
                                ".3gp"
                            ) == true
                        ) {
                            Utils.getBitmapFromURL(
                                getMessageList[index].url,
                                this@ChatActivity
                            ).toString()
                        } else {
                            getMessageList[index].url.toString()
                        }
                    if (!getMessageList[index].url.isNullOrEmpty()) {
                        attachmentList.add(getMessageList[index].url.toString())
                    }
                    chatMessageModel = ChatMessage(
                        myUserId.toString(),
                        getMessageList[index].id.toString(),
                        getMessageList[index].senderId.toString(),
                        getMessageList[index].threadId.toString(),
                        getMessageList[index].message.toString(),
                        getMessageList[index].dateSent.toString(),
                        "",
                        fileName
                    )
                    chatViewModel.insert(chatMessageModel!!)
                }

                this.threadId = if (threadId.isEmpty() || threadId == "null") {
                    getMessageList[0].threadId.toString()
                } else {
                    threadId
                }
                txtMessage.text = null
                Handler(Looper.getMainLooper()).postDelayed({
                    observeMessages(threadId)
                }, 500)
            }
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

        viewModel.getAllMessages(
            threadId,
            recipientsIds?.get(0).toString(),
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()
        )
    }

    private fun getGroupMessageList(threadId: String) {
        viewModel.allGroupMessages.observe(this) {
            if (it.getGroupMessageList?.messages?.isNotEmpty() == true) {
                getGroupMessageList =
                    it.getGroupMessageList.messages as MutableList<GetGroupMessageListQuery.Message>
                var chatMessageModel: ChatMessage? = null
                getGroupMessageList.forEachIndexed { index, _ ->
                    chatMessageModel = ChatMessage(
                        myUserId.toString(),
                        getGroupMessageList[index].id.toString(),
                        getGroupMessageList[index].senderId.toString(),
                        getGroupMessageList[index].threadId.toString(),
                        getGroupMessageList[index].message.toString(),
                        getGroupMessageList[index].dateSent.toString(),
                        getGroupMessageList[index].userName.toString(),
                        getGroupMessageList[index].url.toString(),
                    )
                    if (!getGroupMessageList[index].url.isNullOrEmpty()) {
                        attachmentList.add(getGroupMessageList[index].url.toString())
                    }
                    chatViewModel.insert(chatMessageModel!!)
                }
                this.threadId = if (threadId.isEmpty() || threadId == "null") {
                    getGroupMessageList[0].threadId.toString()
                } else {
                    threadId
                }
                txtMessage.text = null
                Handler(Looper.getMainLooper()).postDelayed({
                    observeMessages(threadId)
                }, 500)
            }
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
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }

        viewModel.getAllGroupMessages(
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()

        )
    }

    override fun onMessageClick(item: ChatMessage?, llOnClick: LinearLayout, adapterPosition: Int) {
        attachmentUrl = item?.url
        shareText = item?.message
        messageIdList.add(item?.id.toString())
        toolbar?.visibility = View.VISIBLE
        linearLayout = llOnClick
        pos = adapterPosition
    }

    override fun onAttachmentClick(item: String?) {
        val intent = Intent(this, ViewPagerAdapterActivity::class.java)
            .putExtra("url", item)
            .putStringArrayListExtra("attachmentList", attachmentList)
        startActivity(intent)
    }

    override fun onDocumentClick(item: String?) {
        if (item.toString().endsWith(".vcf")) {
            runOnUiThread {
                customProgressDialog?.show(this, "")
            }
            val vcfFile = Utils.downloadURL(URL(item))
            Handler(Looper.getMainLooper()).postDelayed({
                readVcf(vcfFile)
            }, 3000)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item))
            browserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(browserIntent)
        }

    }

    override fun onMessageDeselect() {
        toolbar?.visibility = View.GONE
    }

    private fun readVcf(vcfFile: String?) {
        try {
            val file = File(vcfFile.toString())
            //val vcards: List<VCard> = Ezvcard.parse(file).all()

            val card = Ezvcard.parse(file).first()
            runOnUiThread {
                customProgressDialog?.hide()
            }
            addContact(card.formattedName.value, card.telephoneNumbers[0].text.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addContact(name: String, address: String) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setType(ContactsContract.Contacts.CONTENT_TYPE)
            .putExtra(ContactsContract.Intents.Insert.PHONE, address)
            .putExtra(ContactsContract.Intents.Insert.NAME, name)
        startActivityExternal(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
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

            R.id.share -> {
                toolbar?.visibility = View.GONE
                linearLayout?.setBackgroundColor(
                    resources.getColor(
                        android.R.color.transparent,
                        theme
                    )
                )
                val file = Utils.downloadURL(URL(attachmentUrl))

                Handler(Looper.getMainLooper()).postDelayed({
                    if (attachmentUrl?.isNotEmpty() == true) {
                        shareFile(shareText, File(file.toString()))
                    } else {
                        shareToOtherApps(shareText)
                    }
                }, 3000)
            }

            R.id.forward -> {
                showContactBottomSheet()
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
        viewModel.deleteMessage.observe(this) {
            runOnUiThread {
                chatViewModel.deleteMessage(messageIdList)
                toast("Message Deleted!")
            }
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
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light, theme))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }
        viewModel.deleteMessage(threadId.toString(), myUserId.toString(), messageIdList)
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

    /*private val typing = Emitter.Listener { data ->
        runOnUiThread {
            val userTyping = data[0] as JSONObject
            txtTyping?.text = userTyping.getString("data").toString()

            if (userTyping.getString("data").toString().isNotEmpty()) {
                txtTyping?.visibility = View.VISIBLE
            } else {
                txtTyping?.visibility = View.GONE
            }
        }
    }*/

    private val getMessage = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val senderId: String
            val text: String
            val currTime: String
            val name: String
            val url: String
            try {
                senderId = data.getString("senderId")
                text = data.getString("text")
                name = data.getString("name")
                currTime = data.getString("currTime")
                url = data.getString("url")
            } catch (e: JSONException) {
                return@Runnable
            }
            //removeTyping(username)
            txtTyping?.text = ""
            txtTyping?.visibility = View.GONE
            if (isGroup) {
                addMessage(senderId, text, currTime, name, url)
            } else {
                addMessage(senderId, text, currTime, "", url)
            }
        })
    }

    private fun addMessage(
        senderId: String,
        message: String,
        currTime: String,
        userName: String,
        url: String
    ) {
        val dateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
        val iterator: MutableIterator<ChatMessage>? = chatMessageList?.iterator()
        var chatMessageModel: ChatMessage? = null

        while (iterator?.hasNext() == true) {
            val c: ChatMessage = iterator.next()
            val time = currTime.ifEmpty {
                dateFormat
            }
            chatMessageModel = ChatMessage(
                myUserId.toString(),
                c.id,
                senderId,
                c.threadId,
                message,
                dateFormat,
                userName,
                url
            )
        }
        chatMessageModel?.let { chatMessageList?.add(it) }
        chatMessageList?.let { chatAdapter?.updateList(it as MutableList<ChatMessage>) }
        rvMessageList?.scrollToPosition(chatMessageList?.size!!.toInt() - 1)
    }


    private fun shareToOtherApps(body: String? = null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    private fun shareFile(body: String?, file: File) {
        val data = FileProvider.getUriForFile(
            this, "$packageName.fileprovider", file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, data)
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    private fun observeUsers() {
        lifecycleScope.launch {
            chatViewModel.getAllUsers(myUserId.toString()).observe(this@ChatActivity) { list ->
                usersList.clear()
                usersList = list as ArrayList<Users>
            }
        }
    }

    private fun showContactBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.dialog_select_contacts, null)
        view.txtTitle.text = "Contacts"
        view.txtNext.text = "Forward"

        view.rvSelectContacts.layoutManager = LinearLayoutManager(this)
        // This will pass the ArrayList to our Adapter
        val adapter = ChatContactListAdapter(this, usersList, this, true)
        // Setting the Adapter with the recyclerview
        view.rvSelectContacts.adapter = adapter
        view.rvSelectContacts.adapter?.notifyDataSetChanged()


        view.txtNext.setOnClickListener {
            if (selectedUsersList.size == 1) {
                forwardMessage(
                    selectedUsersList[0]?.threadId.toString(),
                    shareText.toString(),
                    selectedUsersList[0]?.userId.toString(),
                    attachmentUrl.toString(),
                    selectedUsersList[0]?.name.toString()
                )
                dialog.dismiss()
            } else if (selectedUsersList.size > 1) {
                toast("Cannot select more than 1 participant!")
            } else {
                toast("Please select a participant!")
            }
        }
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun forwardMessage(
        threadId: String,
        message: String,
        receiverId: String,
        url: String,
        name: String
    ) {
        val list = ArrayList<String>()
        list.add(receiverId)

        mSocket?.emit(
            "sendMessage", currentUserId,
            list,
            message, myUserName, url
        )

        addMessage(currentUserId.toString(), message, "", "", url)

        viewModel.forwardMessage.observe(this) {
            LogHelper.e("CHATACTIVITY", "==== ${it?.forwardMessage?.threadId}")
            val intent = Intent(this, ChatActivity::class.java)
                .putExtra("currentUserId", PreferenceHelper.getStringPreference(this, Constants.USERID))
                .putExtra("threadId", it?.forwardMessage?.threadId)
                .putExtra("userName", name)
                .putStringArrayListExtra("receiverUserId",
                    list as ArrayList<String>?
                )
            startActivity(intent)
            overridePendingTransition(0, 0)
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

        viewModel.forwardMessage(
            message,
            threadId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            receiverId,
            url,
        )
    }

    override fun onChatContactClick(item: Users?) {
    }

    override fun onCheckClick(item: Users?, itemUser: Users?) {
        if (item == null) {
            if (selectedUsersList.contains(itemUser)) {
                selectedUsersList.remove(itemUser)
            }
        } else {
            selectedUsersList.add(item)
        }
    }
}