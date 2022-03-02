package com.sms.moLotus.feature.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.customview.CustomStringBuilder
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.intro.AppIntroActivity
import com.sms.moLotus.feature.main.MainActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.txtMchat
import kotlinx.android.synthetic.main.intro_activity_main.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        CustomStringBuilder.mChatBuilder(txtMchat)


        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler().postDelayed({
            if (!PreferenceHelper.getPreference(this, "INTRO")) {
                val intent = Intent(this, AppIntroActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }  else if (PreferenceHelper.getPreference(this, "UserLoggedIn")) {
                if (!PreferenceHelper.getPreference(this,"APNSETTINGS")) {
                    val intent = Intent(this, APNDetailsActivity::class.java)
                    intent.putExtra(
                        "PhoneNumber",
                        PreferenceHelper.getStringPreference(this, "PhoneNumber")
                    )
                    intent.putExtra(
                        "CarrierText",
                        PreferenceHelper.getStringPreference(this, "CarrierText")
                    )
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
        }, 2000)


        /*val appSignatureHelper = AppSignatureHelper(this)
        appSignatureHelper.appSignatures

        Log.e("===========", "HASH key:::::: ${appSignatureHelper.appSignatures}")*/
    }
}