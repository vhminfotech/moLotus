package com.sms.moLotus.feature.chat.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetUserUsingAppQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.customview.CustomProgressDialog
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.LogHelper
import com.sms.moLotus.feature.chat.adapter.ChatContactListAdapter
import com.sms.moLotus.feature.chat.listener.OnChatContactClickListener
import com.sms.moLotus.feature.chat.model.Users
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.viewmodel.ChatViewModel
import kotlinx.android.synthetic.main.activity_chat_contact_list.*
import kotlinx.android.synthetic.main.dialog_select_contacts.view.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatContactListActivity : AppCompatActivity(), OnChatContactClickListener {

    var contactList: List<String> = ArrayList()
    var usersList: ArrayList<Users> = ArrayList()
    lateinit var viewModel: MainViewModel
    var userId: String = ""
    private lateinit var chatViewModel: ChatViewModel
    private var customProgressDialog: CustomProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_contact_list)
        userId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        customProgressDialog = CustomProgressDialog(this)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        txtTitle.text = "Chat Contacts List"
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        imgRefresh?.visibility = View.VISIBLE

        imgRefresh.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                getContactList()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                getUserUsingAppList(contactList.sorted())
            }, 15000)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            observeUsers()
        }, 500)

        if (!PreferenceHelper.getPreference(this, "isApiCalled")) {
            GlobalScope.launch(Dispatchers.IO) {
                getContactList()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                getUserUsingAppList(contactList.sorted())
            }, 15000)
        }

        txtCreateGroup?.setOnClickListener {
            showBottomSheet()
        }
    }

    @SuppressLint("Range")
    private fun getContactList() {
        runOnUiThread {
            customProgressDialog?.show(this,"Fetching Contacts. Please Wait...")
        }

        val allContactList: ArrayList<String> = ArrayList()

        val cr: ContentResolver = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                /*val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )*/
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )

                        phoneNo.replace("\\s".toRegex(), "")
                        phoneNo.replace("\\s+", "")
                        phoneNo.replace("+", "")
                        phoneNo.replace(" ", "")

                        allContactList.add(
                            phoneNo.replace(" ", "").replace("+", "").replace("-", "").trim()
                        )

                        contactList = allContactList.distinct().toList()

                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        LogHelper.e(
            "CHATCONTACT",
            "contactList: $contactList == ${contactList.size}"
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView(
        contactList: ArrayList<Users>,
        recyclerView: RecyclerView,
        isBottomSheet: Boolean
    ) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        // This will pass the ArrayList to our Adapter
        val adapter = ChatContactListAdapter(this, contactList, this, isBottomSheet)
        // Setting the Adapter with the recyclerview
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun observeUsers() {
        usersList = ArrayList()
        lifecycleScope.launch {
            chatViewModel.getAllUsers(userId).observe(this@ChatContactListActivity) { list ->
                usersList.clear()
                usersList = list as ArrayList<Users>
                if (list.isNotEmpty()) {
                    txtNoData?.visibility = View.GONE
                    rvContact?.visibility = View.VISIBLE
                    initRecyclerView(usersList, rvContact, false)
                } else {
                    txtNoData?.visibility = View.VISIBLE
                    rvContact?.visibility = View.GONE
                }
            }
        }
    }

    private fun getUserUsingAppList(contactList: List<String>) {
        viewModel.userUsingApp.observe(this) {
            PreferenceHelper.setPreference(this, "isApiCalled", true)
            val list: ArrayList<GetUserUsingAppQuery.UserDatum> =
                it.getUserUsingApp?.userData as ArrayList<GetUserUsingAppQuery.UserDatum>

            var userData: Users? = null

            list.forEachIndexed { index, _ ->
                userData = Users(
                    0,
                    userId,
                    list[index].name.toString(),
                    list[index].userId.toString(),
                    list[index].msisdn.toString(),
                    list[index].operator.toString(),
                    list[index].threadId.toString(),
                )
                chatViewModel.deleteUsersTable()
                chatViewModel.insertAllUsers(userData!!)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                observeUsers()
            }, 500)

            runOnUiThread {
                customProgressDialog?.hide()
            }
        }
        viewModel.errorMessage.observe(this) {
            runOnUiThread {
                customProgressDialog?.hide()
            }
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
        viewModel.getUserUsingAppList(
            userId, contactList
        )
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onChatContactClick(item: Users?) {
        val receiverUserIdList = ArrayList<String>()
        receiverUserIdList.add(item?.userId.toString())
        val intent = Intent(this, ChatActivity::class.java)
            .putExtra("currentUserId", userId)
            .putStringArrayListExtra("receiverUserId", receiverUserIdList)
            .putExtra("threadId", item?.threadId.toString())
            .putExtra("userName", item?.name.toString())
            .putExtra("flag", true)
            .putExtra("isChatContact", true)
            .putExtra("isGroup", false)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    var selectedUsersList: ArrayList<Users?> = ArrayList()


    override fun onCheckClick(item: Users?, itemUser: Users?) {
        if (item == null) {
            if (selectedUsersList.contains(itemUser)) {
                selectedUsersList.remove(itemUser)
            }
        } else {
            selectedUsersList.add(item)
        }
    }


    private fun showBottomSheet() {
        val selectedIdsList: ArrayList<String> = ArrayList()
        val selectedNameList: ArrayList<String> = ArrayList()
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)

        // on below line we are inflating a layout file which we have created.
        val view = layoutInflater.inflate(R.layout.dialog_select_contacts, null)
        view.txtTitle.text = "New Group"
        view.txtNext.text = "Next"
        initRecyclerView(usersList, view.rvSelectContacts, true)
        view.txtNext.setOnClickListener {
            if (selectedUsersList.size >= 1) {
                selectedUsersList.forEachIndexed { index, _ ->
                    selectedIdsList.add(selectedUsersList[index]?.userId.toString())
                    selectedNameList.add(selectedUsersList[index]?.name.toString())
                }
                val intent = Intent(this, NewGroupActivity::class.java)
                    .putStringArrayListExtra("selectedIdsList", selectedIdsList)
                    .putStringArrayListExtra("selectedNameList", selectedNameList)
                startActivity(intent)
                finish()
                overridePendingTransition(0, 0)
                dialog.dismiss()

            } else {
                toast("Please select a participant!")
            }
        }
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

}