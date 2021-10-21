package com.sms.moLotus.feature.Intro

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.R
import com.sms.moLotus.feature.main.MainActivity
import kotlinx.android.synthetic.main.apn_details_activity.*
import kotlinx.android.synthetic.main.intro_activity_main.*

class APNDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apn_details_activity)

        //get data from intent
        var intent = intent
        val PhoneNumber = intent.getStringExtra("PhoneNumber")
        val CarrierText = intent.getStringExtra("CarrierText")

//        Toast.makeText(this, "Number: $PhoneNumber \nCarrier: $CarrierText", Toast.LENGTH_LONG).show()

        val APN_et = findViewById<View>(R.id.APN_et) as EditText
        val username_et = findViewById<View>(R.id.username_et) as EditText
        val password_et = findViewById<View>(R.id.password_et) as EditText
        val MMSC_et = findViewById<View>(R.id.MMSC_et) as EditText
        val mms_proxy_et = findViewById<View>(R.id.mms_proxy_et) as EditText

        if(CarrierText == "Telkomsel") {
            APN_et.setText("indosatmms")
            username_et.setText("indosat")
            password_et.setText("")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19:8080")
        }

        if(CarrierText == "Indosat") {
            APN_et.setText("indosatmms")
            username_et.setText("indosat")
            password_et.setText("")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19:8080")
        }

        if(CarrierText == "XL Axiata") {
            APN_et.setText("indosatmms")
            username_et.setText("indosat")
            password_et.setText("")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19:8080")
        }

        APN_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.APN_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "APN copied to clipboard", Toast.LENGTH_LONG).show()
        }

        username_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.username_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Username copied to clipboard", Toast.LENGTH_LONG).show()
        }

        password_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.password_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Password copied to clipboard", Toast.LENGTH_LONG).show()
        }

        MMSC_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MMSC_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MMSC copied to clipboard", Toast.LENGTH_LONG).show()
        }

        mms_proxy_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.mms_proxy_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MMS Proxy copied to clipboard", Toast.LENGTH_LONG).show()
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

            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent)

        }

    }

    fun copy2clipboard(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip);
    }
}