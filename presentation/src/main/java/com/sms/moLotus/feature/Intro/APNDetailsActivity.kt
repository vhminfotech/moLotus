package com.sms.moLotus.feature.Intro

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.R

class APNDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apn_details_activity)

        //get data from intent
        val intent = intent
        val PhoneNumber = intent.getStringExtra("PhoneNumber")
        val CarrierText = intent.getStringExtra("CarrierText")

//        Toast.makeText(this, "Number: $PhoneNumber \nCarrier: $CarrierText", Toast.LENGTH_LONG).show()

        //textview
        val resultTv = findViewById<TextView>(R.id.resultTv)
        //setText
        resultTv.text = "PhoneNumber: "+PhoneNumber+"\nCarrierText: "+CarrierText

    }
}