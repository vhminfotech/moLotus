package com.sms.moLotus.feature.chat.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sms.moLotus.R
import com.sms.moLotus.feature.chat.adapter.GroupParticipantsAdapter
import kotlinx.android.synthetic.main.activity_group_details.*


class GroupDetailsActivity : AppCompatActivity() {

    val list: ArrayList<String> = ArrayList()
    var groupParticipantsAdapter: GroupParticipantsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)
        list.add("Akanksha")
        list.add("Dinesh")
        list.add("Drashti")
        list.add("Nikunj")
        list.add("Abhishek")
        list.add("Jay")
        list.add("Jitesh")
        list.add("Gurudas")

        initRecyclerView(list)

    }

    private fun initRecyclerView(list: List<String>) {
        val layoutMgr = LinearLayoutManager(this)
        layoutMgr.stackFromEnd = true
        rvParticipants.layoutManager = layoutMgr
        groupParticipantsAdapter = GroupParticipantsAdapter(this, list.toMutableList())
        rvParticipants.adapter = groupParticipantsAdapter
        rvParticipants.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

    }

}