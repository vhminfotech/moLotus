package com.sms.moLotus.feature.chat

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetUserUsingAppQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.ChatContactListAdapter
import com.sms.moLotus.feature.chat.listener.OnChatContactClickListener
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_chat_contact_list.*
import kotlinx.android.synthetic.main.layout_header.*

class ChatContactListActivity : AppCompatActivity(), OnChatContactClickListener {

    var contactList = ArrayList<String>()
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_contact_list)
        userId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        Log.e("==========", "userId :: $userId")
        viewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
                MainViewModel::class.java
            )
        txtTitle.text = "Chat Contacts List"
        imgBack?.setOnClickListener {
            onBackPressed()
        }

        getContactList()

    }

    @SuppressLint("Range")
    private fun getContactList() {
        val cr: ContentResolver = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
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
                        Log.i("CHATCONTACT", "Name: $name")
                        Log.i(
                            "CHATCONTACT",
                            "Phone Number: $phoneNo"
                        )
                        contactList.add(phoneNo.replace("\\s".toRegex(), ""))
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()


        Log.e(
            "CHATCONTACT",
            "contactList: $contactList"
        )
        getUserUsingAppList(contactList)
//        initRecyclerView(contactList)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView(contactList: ArrayList<GetUserUsingAppQuery.UserDatum>) {
        rvContact?.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        val adapter = ChatContactListAdapter(this, contactList, this)

        // Setting the Adapter with the recyclerview
        rvContact?.adapter = adapter
        rvContact?.adapter?.notifyDataSetChanged()
    }


    private fun getUserUsingAppList(contactList: ArrayList<String>) {
        viewModel.userUsingApp.observe(this, {
            Log.e("=====", "response:: ${it.getUserUsingApp?.userData}")
            val list: ArrayList<GetUserUsingAppQuery.UserDatum> =
                it.getUserUsingApp?.userData as ArrayList<GetUserUsingAppQuery.UserDatum>
            // initRecyclerView(it)
            if (list.size > 0) {
                txtNoData?.visibility = View.GONE
                rvContact?.visibility = View.VISIBLE
                initRecyclerView(list)
            } else {
                txtNoData?.visibility = View.VISIBLE
                rvContact?.visibility = View.GONE
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
        viewModel.getUserUsingAppList(
            userId, contactList
        )
    }

    override fun onChatContactClick(item: GetUserUsingAppQuery.UserDatum?) {
        val intent = Intent(this, ChatActivity::class.java)
            .putExtra("currentUserId", userId)
            .putExtra("receiverUserId", item?.userId.toString())
            .putExtra("threadId", "")
            .putExtra("userName", item?.name.toString())
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}