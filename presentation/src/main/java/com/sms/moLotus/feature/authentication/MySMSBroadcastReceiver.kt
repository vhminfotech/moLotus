package com.sms.moLotus.feature.authentication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern


/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class MySMSBroadcastReceiver : BroadcastReceiver() {
    
    private var otpReceiveListener: OTPReceiveListener? = null
    fun init(otpReceiveListener: OTPReceiveListener?) {
        this.otpReceiveListener = otpReceiveListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                if (extras != null) {
                    val status = extras[SmsRetriever.EXTRA_STATUS] as Status?
                    if (status != null) when (status.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            // Get SMS message contents
                            val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?

                            Log.e("===========","message:: $message")
                            if (message != null) {
                                val pattern = Pattern.compile("(\\d{6})")
                                //   \d is for a digit
                                //   {} is the number of digits here 4.
                                val matcher = pattern.matcher(message)
                                var otp: String? = null
                                if (matcher.find()) {
                                    otp = matcher.group(0) // 6 digit number
                                    if (otpReceiveListener != null) otpReceiveListener?.onOTPReceived(
                                        otp
                                    )
                                } else {
                                    if (otpReceiveListener != null) otpReceiveListener?.onOTPReceived(
                                        null
                                    )
                                }
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> if (otpReceiveListener != null) otpReceiveListener?.onOTPTimeOut()
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
            Log.e("===========","error:: ${e.message}")

        }

    }

    interface OTPReceiveListener {
        fun onOTPReceived(otp: String?)
        fun onOTPTimeOut()
    }
}