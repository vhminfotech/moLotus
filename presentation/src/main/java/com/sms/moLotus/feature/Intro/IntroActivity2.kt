package com.sms.moLotus.feature.Intro

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.intro_activity_main.*

class IntroActivity2 : AppCompatActivity(){

    var languages = mutableListOf("Airtel", "VI", "JIO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity_main)

        languages.add(0,"Select carrier provider")

        // get reference to all views
        var inputPhoneNumber = findViewById<EditText>(R.id.phone_number)
        var btnLogin = findViewById<Button>(R.id.btnLogin)

        val adapter:ArrayAdapter<String> = object: ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        ){
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view:TextView = super.getDropDownView(
                    position,
                    convertView,
                    parent
                ) as TextView

                // set item text bold
                view.setTypeface(view.typeface, Typeface.BOLD)
                view.textAlignment = View.TEXT_ALIGNMENT_CENTER
                view.gravity = Gravity.CENTER

                // set selected item style
                if (position == carrier_provider.selectedItemPosition && position != 0 ){
//                    view.background = ColorDrawable(Color.parseColor("#F7E7CE"))
//                    view.setTextColor(Color.parseColor("#333399"))
                }

                // make hint item color gray
                if(position == 0){
                    view.setTextColor(Color.LTGRAY)
                }

                return view
            }
            override fun isEnabled(position: Int): Boolean {
                // disable first item
                // first item is display as hint
                return position != 0
            }
        }

        // finally, data bind spinner with adapter
        carrier_provider.adapter = adapter

        // spinner on item selected listener
        carrier_provider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // by default spinner initial selected item is first item
                if (position != 0){
//                    showToast(message = "Position:${position} and language: ${languages[position]}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                showToast(message = "Nothing selected")
            }
        }

        // set on-click listener
        btnLogin.setOnClickListener {
            val PhoneNumber = inputPhoneNumber.text
            val CarrierText = carrier_provider.selectedItem.toString()

            Toast.makeText(this, "Number: $PhoneNumber \nCarrier: $CarrierText", Toast.LENGTH_LONG).show()
        }
    }

    private fun showToast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }
}

