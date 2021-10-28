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
        val NAME_et = findViewById<View>(R.id.NAME_et) as EditText
        val APN_et = findViewById<View>(R.id.APN_et) as EditText
        val Proxy_et = findViewById<View>(R.id.Proxy_et) as EditText
        val Port_et = findViewById<View>(R.id.Port_et) as EditText
        val username_et = findViewById<View>(R.id.username_et) as EditText
        val password_et = findViewById<View>(R.id.password_et) as EditText
        val Server_et = findViewById<View>(R.id.Server_et) as EditText
        val MMSC_et = findViewById<View>(R.id.MMSC_et) as EditText
        val mms_proxy_et = findViewById<View>(R.id.mms_proxy_et) as EditText
        val MMS_Port_et = findViewById<View>(R.id.MMS_Port_et) as EditText
        val MCC_et = findViewById<View>(R.id.MCC_et) as EditText
        val MNC_et = findViewById<View>(R.id.MNC_et) as EditText
        val Auth_Type_et = findViewById<View>(R.id.Auth_Type_et) as EditText
        val APN_Type_et = findViewById<View>(R.id.APN_Type_et) as EditText
        val APN_Protocol_et = findViewById<View>(R.id.APN_Protocol_et) as EditText
        val APN_Roaming_et = findViewById<View>(R.id.APN_Roaming_et) as EditText
        val Bearer_et = findViewById<View>(R.id.Bearer_et) as EditText
        val MVNO_Type_et = findViewById<View>(R.id.MVNO_Type_et) as EditText
        val MVNO_Value_et = findViewById<View>(R.id.MVNO_Value_et) as EditText

        if(CarrierText == "Telkomsel") {
            NAME_et.setText("Telkomsel MMS")
            APN_et.setText("indosatmms")
            Proxy_et.setText("Not Set")
            Port_et.setText("Not Set")
            username_et.setText("indosat")
            password_et.setText("indosat")
            Server_et.setText("Not Set")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19")
            MMS_Port_et.setText("8080")
            MCC_et.setText("510")
            MNC_et.setText("01")
            Auth_Type_et.setText("Not Set")
            APN_Type_et.setText("mms")
            APN_Protocol_et.setText("IPv4")
            APN_Roaming_et.setText("IPv4")
            Bearer_et.setText("unspecified")
            MVNO_Type_et.setText("none")
            MVNO_Value_et.setText("Not set")
        }

        if(CarrierText == "Indosat") {
            NAME_et.setText("Indosat MMS")
            APN_et.setText("indosatmms")
            Proxy_et.setText("Not Set")
            Port_et.setText("Not Set")
            username_et.setText("indosat")
            password_et.setText("indosat")
            Server_et.setText("Not Set")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19")
            MMS_Port_et.setText("8080")
            MCC_et.setText("510")
            MNC_et.setText("01")
            Auth_Type_et.setText("Not Set")
            APN_Type_et.setText("mms")
            APN_Protocol_et.setText("IPv4")
            APN_Roaming_et.setText("IPv4")
            Bearer_et.setText("unspecified")
            MVNO_Type_et.setText("none")
            MVNO_Value_et.setText("Not set")
        }

        if(CarrierText == "XL Axiata") {
            NAME_et.setText("XL Axiata MMS")
            APN_et.setText("indosatmms")
            Proxy_et.setText("Not Set")
            Port_et.setText("Not Set")
            username_et.setText("indosat")
            password_et.setText("indosat")
            Server_et.setText("Not Set")
            MMSC_et.setText("http://mmsc.indosat.com")
            mms_proxy_et.setText("10.19.19.19")
            MMS_Port_et.setText("8080")
            MCC_et.setText("510")
            MNC_et.setText("01")
            Auth_Type_et.setText("Not Set")
            APN_Type_et.setText("mms")
            APN_Protocol_et.setText("IPv4")
            APN_Roaming_et.setText("IPv4")
            Bearer_et.setText("unspecified")
            MVNO_Type_et.setText("none")
            MVNO_Value_et.setText("Not set")
        }

        NAME_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.NAME_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "NAME copied to clipboard", Toast.LENGTH_LONG).show()
        }

        APN_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.APN_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "APN copied to clipboard", Toast.LENGTH_LONG).show()
        }

        Proxy_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.Proxy_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Proxy copied to clipboard", Toast.LENGTH_LONG).show()
        }

        Port_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.Port_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Port copied to clipboard", Toast.LENGTH_LONG).show()
        }

        username_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.username_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Username copied to clipboard", Toast.LENGTH_LONG).show()
        }

        Server_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.Server_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Server copied to clipboard", Toast.LENGTH_LONG).show()
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

        MMS_Port_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MMS_Port_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MMS Port copied to clipboard", Toast.LENGTH_LONG).show()
        }

        MCC_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MCC_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MCC copied to clipboard", Toast.LENGTH_LONG).show()
        }

        MNC_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MNC_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MNC copied to clipboard", Toast.LENGTH_LONG).show()
        }

        Auth_Type_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.Auth_Type_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Authentication Type copied to clipboard", Toast.LENGTH_LONG).show()
        }

        APN_Type_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.APN_Type_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "APN Type copied to clipboard", Toast.LENGTH_LONG).show()
        }

        APN_Protocol_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.APN_Protocol_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "APN Protocol copied to clipboard", Toast.LENGTH_LONG).show()
        }

        APN_Roaming_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.APN_Roaming_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "APN Roaming copied to clipboard", Toast.LENGTH_LONG).show()
        }

        Bearer_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.Bearer_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "Bearer copied to clipboard", Toast.LENGTH_LONG).show()
        }

        MVNO_Type_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MVNO_Type_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MVNO Type copied to clipboard", Toast.LENGTH_LONG).show()
        }

        MVNO_Value_tf.setEndIconOnClickListener {
            val text = findViewById<View>(R.id.MVNO_Value_et) as EditText
            val value = text.text.toString()
            copy2clipboard(value)
            Toast.makeText(this, "MVNO Value copied to clipboard", Toast.LENGTH_LONG).show()
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
        val clip = ClipData.newPlainText("text", text.toString())
        clipboard.setPrimaryClip(clip);
    }
}