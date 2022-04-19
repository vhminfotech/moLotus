package com.sms.moLotus.feature.intro

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.activity_app_intro.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AppIntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_intro)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        GlobalScope.launch(Dispatchers.IO){
            getAppId()
        }
        llGetStarted?.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java);
            startActivity(intent)
            finish()
            /*val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()*/
        }
    }

    private fun getAppId() {
        var adInfo: AdvertisingIdClient.Info? = null
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val androidId: String? = adInfo?.id
        Timber.d("DEVICE_ID :: $androidId")
        PreferenceHelper.setStringPreference(this, "GOOGLE_ADVERTISING_ID", androidId.toString())
    }
}