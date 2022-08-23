package com.sms.moLotus.feature.intro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.main.MainActivity
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.apn_details_activity.*

class APNDetailsActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()


    private fun getApnDetails(carrierId: Int) {

        viewModel.apnDetails.observe(this) {
//            Log.e("=====", "response:: $it")
            etName.setText(it.getApnDetails?.apnName)
            etApn.setText(it.getApnDetails?.apn)
            etProxy.setText(it.getApnDetails?.proxy)
            etPort.setText(it.getApnDetails?.port)
            etUserName.setText(it.getApnDetails?.userName)
            etPassword.setText(it.getApnDetails?.password)
            etServer.setText(it.getApnDetails?.server)
            etMMSC.setText(it.getApnDetails?.mmsc)
            etMmsProxy.setText(it.getApnDetails?.mmsProxy)
            MMS_Port_et.setText(it.getApnDetails?.mmsPort)
            MCC_et.setText(it.getApnDetails?.mcc)
            MNC_et.setText(it.getApnDetails?.mnc)
            Auth_Type_et.setText(it.getApnDetails?.authType)
            APN_Type_et.setText(it.getApnDetails?.apnType)
            if (it.getApnDetails?.apnProtocol?.isEmpty() == true) {
                APN_Protocol_et.setText("1Pv4/1Pv6")
            } else {
                APN_Protocol_et.setText(it.getApnDetails?.apnProtocol)
            }

            APN_Roaming_et.setText(it.getApnDetails?.apnRoaming)
            Bearer_et.setText(it.getApnDetails?.bearer)
            MVNO_Type_et.setText(it.getApnDetails?.mvnoType)
            MVNO_Value_et.setText(it.getApnDetails?.mvnoValue)
        }
        viewModel.errorMessage.observe(this) {
//            Log.e("=====", "errorMessage:: $it")


            val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.introlinearlayout),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {
                        viewModel.getApnDetails(carrierId)
                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        }
        viewModel.getApnDetails(carrierId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apn_details_activity)
        //get data from intent
        var intent = intent
        val phoneNumber = intent.getStringExtra("PhoneNumber")
        if (!PreferenceHelper.getPreference(this, "isSettings")) {
            skip.visibility = View.VISIBLE
        }else{
            skip.visibility = View.GONE
        }
        viewModel =
            ViewModelProvider(this/*, MyViewModelFactory(MainRepository(retrofitService))*/).get(
                MainViewModel::class.java
            )
        getApnDetails(Constants.CARRIER_ID)

        NAME_tf.setEndIconOnClickListener {
            copy2clipboard(etName.text.toString())
            toast("NAME copied to clipboard")
        }

        APN_tf.setEndIconOnClickListener {
            copy2clipboard(etApn.text.toString())
            toast("APN copied to clipboard")
        }

        Proxy_tf.setEndIconOnClickListener {
            copy2clipboard(etProxy.text.toString())
            toast("Proxy copied to clipboard")
        }

        Port_tf.setEndIconOnClickListener {
            copy2clipboard(etPort.text.toString())
            toast("Port copied to clipboard")
        }

        username_tf.setEndIconOnClickListener {
            copy2clipboard(etUserName.text.toString())
            toast("Username copied to clipboard")
        }

        Server_tf.setEndIconOnClickListener {
            copy2clipboard(etServer.text.toString())
            toast("Server copied to clipboard")
        }

        password_tf.setEndIconOnClickListener {
            copy2clipboard(etPassword.text.toString())
            toast("Password copied to clipboard")
        }

        MMSC_tf.setEndIconOnClickListener {
            copy2clipboard(etMMSC.text.toString())
            toast("MMSC copied to clipboard")
        }

        mms_proxy_tf.setEndIconOnClickListener {
            copy2clipboard(etMmsProxy.text.toString())
            toast("MMS Proxy copied to clipboard")
        }

        MMS_Port_tf.setEndIconOnClickListener {
            copy2clipboard(MMS_Port_et.text.toString())
            toast("MMS Port copied to clipboard")
        }

        MCC_tf.setEndIconOnClickListener {
            copy2clipboard(MCC_et.text.toString())
            toast("MCC copied to clipboard")
        }

        MNC_tf.setEndIconOnClickListener {
            copy2clipboard(MNC_et.text.toString())
            toast("MNC copied to clipboard")
        }

        Auth_Type_tf.setEndIconOnClickListener {
            copy2clipboard(Auth_Type_et.text.toString())
            toast("Authentication Type copied to clipboard", Toast.LENGTH_LONG)
        }

        APN_Type_tf.setEndIconOnClickListener {
            copy2clipboard(APN_Type_et.text.toString())
            toast("APN Type copied to clipboard")
        }

        APN_Protocol_tf.setEndIconOnClickListener {
            copy2clipboard(APN_Protocol_et.text.toString())
            toast("APN Protocol copied to clipboard")
        }

        APN_Roaming_tf.setEndIconOnClickListener {
            copy2clipboard(APN_Roaming_et.text.toString())
            toast("APN Roaming copied to clipboard")
        }

        Bearer_tf.setEndIconOnClickListener {
            copy2clipboard(Bearer_et.text.toString())
            toast("Bearer copied to clipboard")
        }

        MVNO_Type_tf.setEndIconOnClickListener {
            copy2clipboard(MVNO_Type_et.text.toString())
            toast("MVNO Type copied to clipboard")
        }

        MVNO_Value_tf.setEndIconOnClickListener {
            copy2clipboard(MVNO_Value_et.text.toString())
            toast("MVNO Value copied to clipboard")
        }

        // set on-click listener
        goto_setting.setOnClickListener {
            val settings = getSharedPreferences("appInfo", 0)
            val editor = settings.edit()
            editor.putBoolean("first_time", false)
            editor.commit()

            intent = Intent(Settings.ACTION_APN_SETTINGS)
            startActivity(intent)
        }

        skip.setOnClickListener {
            val settings = getSharedPreferences("appInfo", 0)
            val editor = settings.edit()
            editor.putBoolean("first_time", false)
            editor.commit()

            PreferenceHelper.setPreference(this, "APNSETTINGS", true)
            PreferenceHelper.setPreference(this, "Notification", true)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (!PreferenceHelper.getPreference(this, "isSettings")) {
            finishAndRemoveTask()
            finishAffinity()
            finish()
        }
    }

    private fun copy2clipboard(text: CharSequence) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text.toString())
        clipboard.setPrimaryClip(clip)
    }
}