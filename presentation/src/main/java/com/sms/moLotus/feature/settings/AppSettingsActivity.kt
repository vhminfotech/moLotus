package com.sms.moLotus.feature.settings

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.intro.IntroActivity2
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.activity_app_settings.*
import kotlinx.android.synthetic.main.layout_add_signature.*
import kotlinx.android.synthetic.main.layout_check_for_updates.*
import kotlinx.android.synthetic.main.layout_header.*

class AppSettingsActivity : AppCompatActivity() {
    lateinit var navigator: Navigator
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    private var versionCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )
        getVersionCodeFromApi()
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        imgBack?.setOnClickListener {
            onBackPressed()
        }

        txtVersion?.text = info.versionName
        toggleSignature?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                openSignatureDialog()
            }
        }

        toggleSendPaidMessages.isChecked = PreferenceHelper.getPreference(this, "SendPaidMessage")
        toggleNotification.isChecked = PreferenceHelper.getPreference(this, "Notification")

        toggleSendPaidMessages?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleSendPaidMessages.isChecked = true
                PreferenceHelper.setPreference(this, "SendPaidMessage", true)
            } else {
                toggleSendPaidMessages.isChecked = false
                PreferenceHelper.setPreference(this, "SendPaidMessage", false)
            }
        }

        toggleNotification?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleNotification.isChecked = true
                PreferenceHelper.setPreference(this, "Notification", true)
            } else {
                toggleNotification.isChecked = false
                PreferenceHelper.setPreference(this, "Notification", false)
            }
        }

        txtAPN?.setOnClickListener {
            PreferenceHelper.setPreference(this, "isSettings", true)
            startActivity(Intent(this, APNDetailsActivity::class.java))
        }

        txtAboutUs?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.molotus.com/"))
            startActivity(intent)
        }

        llCheckForUpdates?.setOnClickListener {
            if (versionCode >= info.versionCode) {
                openAppUpdateDialog()
            } else {
                Toast.makeText(this, "No Update Available!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSignatureDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_add_signature)

        val window: Window? = dialog.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        wlp?.width = FrameLayout.LayoutParams.MATCH_PARENT
        wlp?.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.attributes = wlp

        dialog.txtOk?.setOnClickListener {
            dialog.dismiss()
            toggleSignature?.isChecked = false
            /* llSignatureText.visibility = View.VISIBLE
             view7.visibility = View.VISIBLE*/
            txtSignatureText?.text = dialog.etSign?.text.toString()
        }
        dialog.txtCancel?.setOnClickListener {
            dialog.dismiss()
            toggleSignature?.isChecked = false
        }
        dialog.show()
    }

    private fun openAppUpdateDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_check_for_updates)

        val window: Window? = dialog.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.CENTER_VERTICAL
        wlp?.width = FrameLayout.LayoutParams.MATCH_PARENT
        wlp?.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window?.attributes = wlp

        dialog.txtUpdate?.setOnClickListener {
            dialog.dismiss()

        }
        dialog.txtRemind?.setOnClickListener {
            dialog.dismiss()

        }
        dialog.show()
    }

    private fun getVersionCodeFromApi() {
        viewModel.versionCode.observe(this, {
            Log.e("=====", "response:: $it")
            this.versionCode = it[0].config_value.toInt()
        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
            Snackbar.make(
                findViewById(R.id.introlinearlayout),
                "No Internet Connection. Please turn on your internet!",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Retry") {
                    viewModel.getVersionCode()
                }
                .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                .show()
        })
        viewModel.getVersionCode()
    }
}