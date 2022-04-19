package com.sms.moLotus.feature.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.feature.intro.APNDetailsActivity
import com.sms.moLotus.feature.intro.AppIntroActivity
import com.sms.moLotus.feature.intro.IntroActivity
import com.sms.moLotus.feature.main.MainActivity
import timber.log.Timber


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //CustomStringBuilder.mChatBuilder(txtMchat)


        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.

        Timber.e("======== ${PreferenceHelper.getPreference(this, "UserLoggedIn")}")
        Handler(Looper.getMainLooper()).postDelayed({
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
                val intent = Intent(this, IntroActivity::class.java)
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