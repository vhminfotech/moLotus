package com.sms.moLotus.feature.intro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.R
import com.sms.moLotus.feature.authentication.VerifyOtpActivity
import com.sms.moLotus.feature.networkcall.ApiHelper
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.intro_activity_main.*
import kotlinx.android.synthetic.main.intro_activity_main.txtMchat

class IntroActivity : AppCompatActivity() {

    var languages = mutableListOf("Telkomsel", "Indosat", "XL Axiata", "Celcom", "U Mobile")
    private var api: ApiHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity_main)
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
        languages.add(0, "Select carrier provider")
        api = ApiHelper(this)
        // get reference to all views
        var inputPhoneNumber = findViewById<EditText>(R.id.phone_number)

        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(
                    position,
                    convertView,
                    parent
                ) as TextView

                // set item text bold
                view.setTypeface(view.typeface, Typeface.BOLD)
                view.textAlignment = View.TEXT_ALIGNMENT_CENTER
                view.gravity = Gravity.CENTER

                // set selected item style
                if (position == carrier_provider.selectedItemPosition && position != 0) {
//                    view.background = ColorDrawable(Color.parseColor("#F7E7CE"))
//                    view.setTextColor(Color.parseColor("#333399"))
                }

                // make hint item color gray
                if (position == 0) {
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
                if (position != 0) {
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

            if (PhoneNumber.isEmpty()) {
                showToast(message = "Phone number is empty")
            }

            if (CarrierText == "Select carrier provider") {
                showToast(message = "Please select carrier provider")
            }

            if (CarrierText != "Select carrier provider" && !PhoneNumber.isEmpty()) {
                requestOtp(PhoneNumber.toString(), CarrierText)
            }
        }
    }

    private fun requestOtp(phoneNo: String,carrierText: String) {
        api?.request(phoneNo,
            { success ->
                if (success) {
                    Toast.makeText(
                        this,
                        "OTP sent successfully.!!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, VerifyOtpActivity::class.java);
                    intent.putExtra("PhoneNumber", phoneNo)
                    intent.putExtra("CarrierText", carrierText)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "Error getting OTP", Toast.LENGTH_LONG
                    ).show()
                }
            }) { // Do something else.
            Toast.makeText(
                this,
                "Failed to request OTP", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showToast(
        context: Context = applicationContext,
        message: String,
        duration: Int = Toast.LENGTH_LONG
    ) {
        Toast.makeText(context, message, duration).show()
    }
}

