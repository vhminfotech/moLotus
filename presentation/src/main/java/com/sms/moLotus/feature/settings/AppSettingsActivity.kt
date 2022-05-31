package com.sms.moLotus.feature.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.extension.checkSelfPermissionCompat
import com.sms.moLotus.extension.requestPermissionsCompat
import com.sms.moLotus.extension.shouldShowRequestPermissionRationaleCompat
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import com.sms.moLotus.feature.settings.autodownloadapk.UpdateApp
import kotlinx.android.synthetic.main.activity_app_settings.*
import kotlinx.android.synthetic.main.layout_add_signature.*
import kotlinx.android.synthetic.main.layout_check_for_updates.*
import kotlinx.android.synthetic.main.layout_header.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class AppSettingsActivity : AppCompatActivity() {
    lateinit var navigator: Navigator
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()
    private var versionCode: Int = 0
    private var apkUrl: String = ""
    private var progressDialog: ProgressDialog? = null
    var webView: WebView? = null

    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)
        progressDialog = ProgressDialog(this)
        webView = WebView(this)

        viewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
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
        toggleAutoDownload.isChecked = PreferenceHelper.getPreference(this, "AutoDownload")

        toggleSendPaidMessages?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleSendPaidMessages.isChecked = true
                PreferenceHelper.setPreference(this, "SendPaidMessage", true)
            } else {
                toggleSendPaidMessages.isChecked = false
                PreferenceHelper.setPreference(this, "SendPaidMessage", false)
            }
        }

        toggleAutoDownload?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                toggleAutoDownload.isChecked = true
                PreferenceHelper.setPreference(this, "AutoDownload", true)
            } else {
                toggleAutoDownload.isChecked = false
                PreferenceHelper.setPreference(this, "AutoDownload", false)
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
            /*val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.molotus.com/"))
            startActivity(intent)*/

            webView?.settings?.javaScriptEnabled = true
            webView?.loadUrl("file:///android_asset/AboutUs.html")
            setContentView(webView)
        }

        llCheckForUpdates?.setOnClickListener {
            if (versionCode > info.versionCode) {
                openAppUpdateDialog()
            } else {
                Toast.makeText(this, "No Update Available!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
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

    @SuppressLint("AutoDispose")
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

            // check storage permission granted if yes then start downloading file
            checkStoragePermission()
            dialog.dismiss()
        }
        dialog.txtRemind?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun convertToHashMap(jsonString: String?): HashMap<String?, String>? {
        val myHashMap = HashMap<String?, String>()
        try {
            val jArray = JSONArray(jsonString)
            Log.e("=====", "jArray:: ${jArray.get(0)}")

            var jObject: JSONObject? = null
            var keyString: String? = null
            for (i in 0 until jArray.length()) {
                jObject = jArray.getJSONObject(i)
                // beacuse you have only one key-value pair in each object so I have used index 0
                keyString = jObject.names().get(i) as String?
                myHashMap[keyString] = jObject.getString(keyString)
            }
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Log.e("=====", "error:: ${e.message}")

        }
        return myHashMap
    }

    private fun getVersionCodeFromApi() {
        viewModel.versionCode.observe(this, {
            val jArray = JSONArray(Gson().toJson(it.appConfigRes))
            this.versionCode = Integer.parseInt(jArray.getJSONObject(0)["configValue"].toString())
            this.apkUrl = jArray.getJSONObject(2)["configValue"].toString()
        })
        viewModel.errorMessage.observe(this, {
            //Log.e("=====", "errorMessage:: $it")
            Snackbar.make(
                findViewById(R.id.mainLayout),
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                runOnUiThread {
                    val updateApp = UpdateApp()
                    updateApp.setContext(this, progressDialog)
                    updateApp.execute(apkUrl)
                }
            } else {
                // Permission request was denied.
                toast(getString(R.string.storage_permission_denied))
            }
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted


        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
            }
        ) {
            // start downloading
            runOnUiThread {
                val updateApp = UpdateApp()
                updateApp.setContext(this, progressDialog)
                updateApp.execute(apkUrl)
            }
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse(String.format("package:%s", applicationContext?.packageName))
            startActivity(intent)
        } else {
            if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                toast(getString(R.string.storage_access_required))
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            } else {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }
        }
    }
}