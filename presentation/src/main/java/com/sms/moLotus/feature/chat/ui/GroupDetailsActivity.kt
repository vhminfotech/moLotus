package com.sms.moLotus.feature.chat.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.GetGroupDetailsQuery
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.chat.adapter.GroupParticipantsAdapter
import com.sms.moLotus.feature.chat.listener.OnGroupItemClickListener
import com.sms.moLotus.feature.retrofit.MainViewModel
import kotlinx.android.synthetic.main.activity_group_details.*
import java.text.SimpleDateFormat
import java.util.*


class GroupDetailsActivity : AppCompatActivity(), OnGroupItemClickListener {
    lateinit var viewModel: MainViewModel
    var groupId = ""
    val list: ArrayList<String> = ArrayList()
    private var groupParticipantsAdapter: GroupParticipantsAdapter? = null
    private var isGroupAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)
        groupId = intent?.getStringExtra("groupId").toString()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        txtExitGroup?.setOnClickListener {
            exitGroup(groupId)
        }

        getGroupDetails(groupId)


    }

    private fun getGroupDetails(groupId: String) {

        viewModel.getGroupDetails.observe(this) {
            txtGroupName?.text = it.getGroupDetails?.groupName.toString()
            txtCreatedAt?.text =
                " ~ created on " + getDate(it.getGroupDetails?.groupCreatedDate.toString())
            if (it.getGroupDetails?.isGroupAdmin?.contains(
                    PreferenceHelper.getStringPreference(
                        this,
                        Constants.USERID
                    )
                ) == true
            ) {
                isGroupAdmin = true
            }
            initRecyclerView(it.getGroupDetails?.participantsOfGroup, isGroupAdmin)
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

        viewModel.getGroupDetails(
            groupId,
            PreferenceHelper.getStringPreference(this, Constants.TOKEN).toString()

        )
    }


    private fun exitGroup(groupId: String) {
        viewModel.exitGroup.observe(this) {
            Snackbar.make(
                llGroupDetails,
                it.exitGroup?.message.toString(),
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
        viewModel.errorMessage.observe(this) {
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.llGroupDetails),
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

        viewModel.exitGroup(
            groupId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString()
        )
    }


    private fun getDate(createdAt: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date? = dateFormat.parse(createdAt)
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return formatter.format(date).toString()
    }

    private fun initRecyclerView(
        list: List<GetGroupDetailsQuery.ParticipantsOfGroup?>?,
        isGroupAdmin: Boolean
    ) {
        val layoutMgr = LinearLayoutManager(this)
        layoutMgr.stackFromEnd = true
        rvParticipants.layoutManager = layoutMgr
        groupParticipantsAdapter = GroupParticipantsAdapter(
            this,
            list as ArrayList<GetGroupDetailsQuery.ParticipantsOfGroup?>?, this, isGroupAdmin
        )
        rvParticipants.adapter = groupParticipantsAdapter
        rvParticipants.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

    }

    override fun onGroupItemClick(
        id: String,
        txt: QkTextView,
        llItem: LinearLayout,
        position: Int
    ) {
        showOptions(llItem, groupId, id, txt, position)
    }

    private fun showOptions(
        view: View,
        groupId: String,
        userId: String,
        txt: QkTextView,
        position: Int
    ) {
        val popup = PopupMenu(this, view, Gravity.END)
        popup.inflate(R.menu.group_options)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.itemGroupAdmin -> {
                    createAdmin(groupId, userId, txt)
                }

                R.id.itemRemoveGroupAdmin -> {
                    removeAdmin(groupId, userId, txt)
                }

                R.id.itemRemoveParticipant -> {

                    removeParticipant(groupId, userId, position)
                }
            }
            true
        }
        popup.show()
    }

    private fun createAdmin(groupId: String, userId: String, txt: QkTextView) {
        txt.visibility = View.VISIBLE
        viewModel.createAdmin.observe(this) {
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
        viewModel.createAdmin(
            groupId,
            userId
        )
    }

    private fun removeAdmin(groupId: String, userId: String, txt: QkTextView) {
        txt.visibility = View.GONE
        viewModel.removeAdmin.observe(this) {
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

        viewModel.removeAdmin(
            groupId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            userId
        )
    }

    private fun removeParticipant(groupId: String, userId: String, position: Int) {
        groupParticipantsAdapter?.removeParticipant(position)
        viewModel.removeParticipant.observe(this) {
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

        viewModel.removeParticipant(
            groupId,
            PreferenceHelper.getStringPreference(this, Constants.USERID).toString(),
            userId
        )
    }
}