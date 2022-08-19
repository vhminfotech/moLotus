package com.sms.moLotus.feature.chat.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.retrofit.MainViewModel
import kotlinx.android.synthetic.main.activity_user_details.*

class UserDetailsActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    var userId = ""
    private var userName = ""
    private var blockUserId = ""
    var blocked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        userId = intent?.getStringExtra("userId").toString()
        userName = intent?.getStringExtra("userName").toString()
        blockUserId = intent?.getStringExtra("blockUserId").toString()
        blocked = intent.getBooleanExtra("blocked", false)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        txtUserName.text = userName
        if (blocked) {
            txtBlockUser.text = "Unblock $userName"
        } else {
            txtBlockUser.text = "Block $userName"
        }

        setListeners()
    }

    private fun setListeners() {
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        txtBlockUser?.setOnClickListener {
            if (txtBlockUser.text.toString() == "Block $userName") {
                blockUser()
            } else {
                unBlockUser()
            }
        }
    }

    private fun blockUser() {
        viewModel.blockUser.observe(this) {
            if(it.blockUser?.error.toString() == "false"){
                toast("User blocked successfully!")
                txtBlockUser.text = "Unblock $userName"
            }
        }
        viewModel.errorMessage.observe(this) {
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.llUserDetails),
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

        viewModel.blockUser(
            userId,
            blockUserId
        )
    }

    private fun unBlockUser() {
        viewModel.unBlockUser.observe(this) {
            if(it.unblockUser?.error.toString() == "false"){
                toast("User unblocked successfully!")
                txtBlockUser.text = "Block $userName"
            }
        }
        viewModel.errorMessage.observe(this) {
            val conMgr =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.llUserDetails),
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

        viewModel.unBlockUser(
            userId,
            blockUserId
        )
    }

}