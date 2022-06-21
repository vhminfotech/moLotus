package com.sms.moLotus.feature.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.AddedContactsAdapter
import kotlinx.android.synthetic.main.activity_new_group.*
import kotlinx.android.synthetic.main.layout_header.*

class NewGroupActivity : AppCompatActivity() {
    var selectedNameList = ArrayList<String>()
    var selectedIdsList = ArrayList<String>()
    var addedContactsAdapter: AddedContactsAdapter? = null
    var userId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)
        userId = PreferenceHelper.getStringPreference(this, Constants.USERID).toString()

        selectedIdsList = intent.getStringArrayListExtra("selectedIdsList") as ArrayList<String>
        selectedNameList = intent.getStringArrayListExtra("selectedNameList") as ArrayList<String>

        LogHelper.e("NewGroupActivity", "selectedIdsList:: $selectedIdsList")
        LogHelper.e("NewGroupActivity", "selectedNameList:: $selectedNameList")

        initRecyclerView(selectedNameList)

        txtTitle.text = "New Group"
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        createGroupChat.setOnClickListener {
            LogHelper.e("NewGroupActivity", "txtGroupName:: ${txtGroupName.text}")
            LogHelper.e("NewGroupActivity", "selectedIdsList:: $selectedIdsList")

            if (!txtGroupName?.text.isNullOrEmpty()) {
                val intent = Intent(this, ChatActivity::class.java)
                    .putExtra("currentUserId", userId)
                    .putStringArrayListExtra("receiverUserId", selectedIdsList)
//                .putExtra("receiverUserId", "")
                    .putExtra("threadId", "")
                    .putExtra("groupName", txtGroupName.text.toString())
                    .putExtra("flag", true)
                    .putExtra("isGroup", true)
                startActivity(intent)
                finish()
                overridePendingTransition(0, 0)
            } else {
                toast("Please enter group name")
            }

        }

    }


    private fun initRecyclerView(list: List<String>) {
        val layoutMgr = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        rvAddedContacts?.hasFixedSize()
        layoutMgr.stackFromEnd = true
        rvAddedContacts.layoutManager = layoutMgr
        addedContactsAdapter = AddedContactsAdapter(this, list.toMutableList())
        rvAddedContacts.adapter = addedContactsAdapter
    }


}