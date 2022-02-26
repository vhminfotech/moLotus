package com.sms.moLotus.feature.authentication

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import com.sms.moLotus.R
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.sms.moLotus.feature.authentication.MySMSBroadcastReceiver.OTPReceiveListener

import com.google.android.gms.tasks.Task
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.networkcall.ApiHelper
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_verify_otp.*
import kotlinx.android.synthetic.main.activity_verify_otp.txtMchat

class VerifyOtpActivity : AppCompatActivity() {
    private var mySMSBroadcastReceiver: MySMSBroadcastReceiver? = null
    private var phoneNo: String? = null
    var carrierText: String? = null
    private var api: ApiHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)
        val word: Spannable = SpannableString("m")

        word.setSpan(
            ForegroundColorSpan(Color.parseColor("#27a9e1")),
            0,
            word.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtMchat.text = word
        val wordTwo: Spannable = SpannableString("Chat")
        wordTwo.setSpan(
            ForegroundColorSpan(Color.parseColor("#ff6b13")),
            0,
            wordTwo.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        txtMchat.append(wordTwo)
        phoneNo = intent?.getStringExtra("PhoneNumber")
        carrierText = intent?.getStringExtra("CarrierText")

        Log.e("===========", "phoneNo::: $phoneNo === carrierText::: $carrierText")
        txtOTP?.text = "Please type the verification code sent to $phoneNo"
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
                Log.e("===========", "OTP Received::: $otp")
                etOTP?.setText(otp)
                etOTP?.text?.length?.let { etOTP?.setSelection(it) }

                if (etOTP.text?.isNotEmpty() == true || etOTP?.text != null || !etOTP.text.isNullOrEmpty()) {
                    verifyOtp(phoneNo.toString(), etOTP.text?.toString().toString())
                }
            }

            override fun onOTPTimeOut() {
                Log.e("===========", "OTP Timeout")
            }
        })

        btnVerifyOtp?.setOnClickListener {
            verifyOtp(phoneNo.toString(), etOTP.text?.toString().toString())
        }
    }

    private fun verifyOtp(requestPhone: String, otp: String) {
        api?.verify(requestPhone, otp,
            { success, phoneNumber ->

                if (success && phoneNumber != null && phoneNumber == requestPhone) {
                    Log.e("===========", "Successfully verified phone number: $phoneNumber")
                    Toast.makeText(
                        this,
                        "Phone no. successfully verified.!!", Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, APNDetailsActivity::class.java);
                    intent.putExtra("PhoneNumber", phoneNumber)
                    intent.putExtra("CarrierText", carrierText)
                    startActivity(intent)
                } else {
                    Log.d("===========", "Unable to verify response.")
                    Toast.makeText(
                        this,
                        "Unable to verify phone no.", Toast.LENGTH_LONG
                    ).show()
                }
            }) {
            Log.d("===========", "Communication error with server.")
            Toast.makeText(
                this,
                "Failed to verify OTP", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener { aVoid ->
            Log.e("===========", "on success")
        }
        task.addOnFailureListener { e ->
            Log.e("===========", "on failure")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mySMSBroadcastReceiver != null) {
            unregisterReceiver(mySMSBroadcastReceiver)
        }
    }
}