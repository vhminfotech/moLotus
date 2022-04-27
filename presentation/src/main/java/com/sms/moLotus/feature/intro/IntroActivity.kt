package com.sms.moLotus.feature.intro

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.authentication.VerifyOtpActivity
import com.sms.moLotus.feature.networkcall.ApiHelper
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.intro_activity_main.*
import javax.inject.Inject


class IntroActivity : AppCompatActivity() {
    @Inject
    lateinit var navigator: Navigator
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
            PreferenceHelper.setStringPreference(this, Constants.TOKEN, it.user_id)
            PreferenceHelper.setStringPreference(this, Constants.TOKEN, it.token)
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
                        /*viewModel.registerUser(
                            etName.text.toString(),
                            Constants.CARRIER_ID,
                            phone_number.text.toString()
                        )*/
                    }
                    .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                    .show()
            } else {
                toast(it.toString(), Toast.LENGTH_SHORT)
            }
        })
        viewModel.registerUser(
            etName.text.toString(),
            Constants.CARRIER_ID,
            phone_number.text.toString()
        )
    }

    fun showDefaultSmsDialog(context: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            context.startActivityForResult(intent, 42389)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity_main)
        //CustomStringBuilder.mChatBuilder(txtMchat)
        showDefaultSmsDialog(this)
        languages.add(0, "Select carrier provider")
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )

//        getOperators()

        api = ApiHelper(this)
        //get reference to all views
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
            } else if (phone_number.text.length <= 7 || phone_number.text.length > 14) {
                btnLogin.isEnabled = true

                toast("Please enter valid phone number having 7 to 14 digits!")
                /*else if (carrierText == "Select carrier provider") {
                                   btnLogin.isEnabled = true
                                   showToast(message = "Please select carrier provider")
                               }*/
            } else if (/*carrierText != "Select carrier provider" &&*/ phone_number.text.isNotEmpty() && phone_number.text.length >= 7 && etName.text.isNotEmpty()) {
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

