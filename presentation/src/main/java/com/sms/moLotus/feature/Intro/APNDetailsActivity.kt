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

        if(CarrierText == "Airtel") {
            APN_et.setText("Airtel APN")
            username_et.setText("Airtel Username")
            password_et.setText("Airtel Password")
            MMSC_et.setText("Airtel MMSC")
            mms_proxy_et.setText("Airtel MMS Proxy")
        }

        if(CarrierText == "VI") {
            APN_et.setText("VI APN")
            username_et.setText("VI Username")
            password_et.setText("VI Password")
            MMSC_et.setText("VI MMSC")
            mms_proxy_et.setText("VI MMS Proxy")
        }

        if(CarrierText == "JIO") {
            APN_et.setText("JIO APN")
            username_et.setText("JIO Username")
            password_et.setText("JIO Password")
            MMSC_et.setText("JIO MMSC")
            mms_proxy_et.setText("JIO MMS Proxy")
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

            intent = Intent(Settings.ACTION_APN_SETTINGS)
            startActivity(intent)

//            Toast.makeText(this, "Number: $PhoneNumber \nCarrier: $CarrierText", Toast.LENGTH_LONG).show()
        }

    }

    fun copy2clipboard(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip);
    }
}