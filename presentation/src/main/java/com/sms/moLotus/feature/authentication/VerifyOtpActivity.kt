package com.sms.moLotus.feature.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.authentication.MySMSBroadcastReceiver.OTPReceiveListener
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.main.MainActivity
import com.sms.moLotus.feature.networkcall.ApiHelper
import kotlinx.android.synthetic.main.activity_verify_otp.*

class VerifyOtpActivity : AppCompatActivity() {
    private var mySMSBroadcastReceiver: MySMSBroadcastReceiver? = null
    private var phoneNo: String? = null

    private var api: ApiHelper? = null
    var isOTPVerified: Boolean? = false

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)
        //CustomStringBuilder.mChatBuilder(txtMchat)

        phoneNo = intent?.getStringExtra("PhoneNumber")
        txtOTP?.text = resources.getString(R.string.otp_desc) + " $phoneNo"
        mySMSBroadcastReceiver = MySMSBroadcastReceiver()
        api = ApiHelper(this)

        registerReceiver(
            mySMSBroadcastReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        )
        startSMSRetrieverClient() // Already implemented above.

        mySMSBroadcastReceiver?.init(object : OTPReceiveListener {
            override fun onOTPReceived(otp: String?) {
                // OTP Received
                //Log.e("===========", "OTP Received::: $otp")
                etOTP?.setText(otp)
                etOTP?.text?.length?.let { etOTP?.setSelection(it) }
                if (etOTP.text?.isNotEmpty() == true || etOTP?.text != null || !etOTP.text.isNullOrEmpty()) {
                    //verifyOtp(phoneNo.toString(), etOTP.text?.toString().toString())
                }
            }

            override fun onOTPTimeOut() {
                isOTPVerified = true
                //Log.e("===========", "OTP Timeout")
            }
        })
        btnResendOtp?.setOnClickListener {
            toast("OTP sent successfully.!!")
            //carrierId?.let { it1 -> requestOtp(phoneNo.toString(), carrierText.toString(), it1) }
        }

        btnVerifyOtp?.setOnClickListener {
            if (etOTP?.text?.toString().equals("123456")) {
                Toast.makeText(
                    this,
                    "Phone no. successfully verified.!!", Toast.LENGTH_SHORT
                ).show()
                PreferenceHelper.setPreference(this, "UserLoggedIn", true)
                PreferenceHelper.setPreference(this, "INTRO", true)
                PreferenceHelper.setStringPreference(this, "PhoneNumber", phoneNo.toString())

                val settings = getSharedPreferences("appInfo", 0)
                val editor = settings.edit()
                editor.putBoolean("first_time", false)
                editor.commit()

                PreferenceHelper.setPreference(this, "APNSETTINGS", true)
                PreferenceHelper.setPreference(this, "Notification", true)
                PreferenceHelper.setPreference(this, "isVerified", true)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("PhoneNumber", phoneNo)
                startActivity(intent)
                finish()


                /*val intent = Intent(this, APNDetailsActivity::class.java)
                intent.putExtra("PhoneNumber", phoneNo)
                startActivity(intent)*/
            } else {
                Toast.makeText(
                    this,
                    "Please enter correct OTP!", Toast.LENGTH_LONG
                ).show()
            }
            //verifyOtp(phoneNo.toString(), etOTP.text?.toString().toString())
        }

        etOTP?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isOTPVerified == true) {
                    if (s?.length == 6) {
                        //verifyOtp(phoneNo.toString(), etOTP.text?.toString().toString())
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        finishAffinity()
        finishAndRemoveTask()
    }


    private fun verifyOtp(requestPhone: String, otp: String) {
        api?.verify(requestPhone, otp,
            { success, phoneNumber ->

                if (success && phoneNumber != null && phoneNumber == requestPhone) {
                    isOTPVerified = true

                    //Log.e("===========", "Successfully verified phone number: $phoneNumber")
                    Toast.makeText(
                        this,
                        "Phone no. successfully verified.!!", Toast.LENGTH_SHORT
                    ).show()
                    PreferenceHelper.setPreference(this, "UserLoggedIn", true)
                    PreferenceHelper.setPreference(this, "INTRO", true)
                    PreferenceHelper.setStringPreference(this, "PhoneNumber", phoneNumber)
                     val intent = Intent(this, APNDetailsActivity::class.java)
                    intent.putExtra("PhoneNumber", phoneNumber)
                    startActivity(intent)
                } else {
                    isOTPVerified = true


                    //Log.d("===========", "Unable to verify response.")
                    Toast.makeText(
                        this,
                        "Please enter correct OTP!", Toast.LENGTH_LONG
                    ).show()
                }
            }) {
            //Log.d("===========", "Communication error with server.")
            isOTPVerified = false
            Toast.makeText(
                this,
                "Failed to verify OTP", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            //Log.e("===========", "on success")
        }
        task.addOnFailureListener {
            //Log.e("===========", "on failure")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mySMSBroadcastReceiver != null) {
            unregisterReceiver(mySMSBroadcastReceiver)
        }
    }
}