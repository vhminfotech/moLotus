package com.sms.moLotus.feature.intro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.R
import com.sms.moLotus.customview.CustomStringBuilder
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.authentication.VerifyOtpActivity
import com.sms.moLotus.feature.networkcall.ApiHelper
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.intro_activity_main.*
import kotlinx.android.synthetic.main.intro_activity_main.txtMchat

class IntroActivity : AppCompatActivity() {

    var languages = mutableListOf<String>()

    //var languages = mutableListOf("Telkomsel", "Indosat", "XL Axiata", "Celcom", "U Mobile")
    private var api: ApiHelper? = null
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()

    private fun getOperators() {
        viewModel.operatorsList.observe(this, {
            Log.e("=====", "response:: $it")
            it.forEach { element ->
                languages.add(element.operator_name)
            }
            Log.e("=====", "languages added:: $languages")
        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
            //Toast.makeText(this, it.toString(),Toast.LENGTH_SHORT).show()
            val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.introlinearlayout),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {
                        viewModel.getAllOperators()
                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })
        viewModel.getAllOperators()
    }

    private fun registerUser() {
        viewModel.loginResponse.observe(this, {
            Log.e("=====", "response:: $it")
            /*requestOtp(
                phone_number.text.toString(),
                carrier_provider.selectedItem.toString(),
                carrier_provider.selectedItemId.toInt()
            )*/
            btnLogin.isEnabled = true
            Toast.makeText(
                this,
                "OTP sent successfully.!!",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, VerifyOtpActivity::class.java)
            intent.putExtra("PhoneNumber", phone_number.text.toString())
            startActivity(intent)
            finish()
        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
            btnLogin.isEnabled = true
            val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = conMgr.activeNetworkInfo
            if (netInfo == null) {
                //No internet
                Snackbar.make(
                    findViewById(R.id.introlinearlayout),
                    "No Internet Connection. Please turn on your internet!",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Retry") {
                        viewModel.registerUser(
                            etName.text.toString(),
                            carrier_provider.selectedItemId.toInt(),
                            phone_number.text.toString()
                        )
                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })
        viewModel.registerUser(
            etName.text.toString(),
            carrier_provider.selectedItemId.toInt(),
            phone_number.text.toString()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity_main)
        CustomStringBuilder.mChatBuilder(txtMchat)

        languages.add(0, "Select carrier provider")
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )



//        getOperators()




        api = ApiHelper(this)
        // get reference to all views
        //val inputPhoneNumber = findViewById<EditText>(R.id.phone_number)

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
                /*if (position == carrier_provider.selectedItemPosition && position != 0) {
                    view.background = ColorDrawable(Color.parseColor("#F7E7CE"))
                    view.setTextColor(Color.parseColor("#333399"))
                }*/

                // make hint item color gray
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.darker_gray))
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
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                (parent.getChildAt(0) as TextView).textSize = 14f

                // by default spinner initial selected item is first item
                /*if (position != 0) {
                    showToast(message = "Position:${position} and language: ${languages[position]}")
                }*/
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                showToast(message = "Nothing selected")
            }
        }

        // set on-click listener
        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            //val phoneNumber = inputPhoneNumber.text
            if (etName.text.isEmpty()) {
                btnLogin.isEnabled = true
                showToast(message = "Name is empty")
            } else if (phone_number.text.isEmpty()) {
                btnLogin.isEnabled = true
                showToast(message = "Phone number is empty")
            } /*else if (carrierText == "Select carrier provider") {
                btnLogin.isEnabled = true
                showToast(message = "Please select carrier provider")
            }*/ else if (/*carrierText != "Select carrier provider" &&*/ phone_number.text.isNotEmpty() && phone_number.text.length == 10 && etName.text.isNotEmpty()) {
                btnLogin.isEnabled = false
                registerUser()
            } else {
                btnLogin.isEnabled = true
                toast("Please enter valid phone number!")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        finish()
    }

    private fun requestOtp(phoneNo: String, carrierText: String, carrierId: Int) {
        api?.request(phoneNo,
            { success ->
                if (success) {
                    btnLogin.isEnabled = true
                    Toast.makeText(
                        this,
                        "OTP sent successfully.!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, VerifyOtpActivity::class.java)
                    intent.putExtra("PhoneNumber", phoneNo)
                    startActivity(intent)
                    finish()
                } else {
                    btnLogin.isEnabled = true
                    Toast.makeText(
                        this,
                        "Error getting OTP", Toast.LENGTH_LONG
                    ).show()
                }
            }) { // Do something else.
            btnLogin.isEnabled = true
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

