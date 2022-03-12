package com.sms.moLotus.feature.intro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.sms.moLotus.R
import com.sms.moLotus.extension.toast
import com.sms.moLotus.feature.retrofit.MainRepository
import com.sms.moLotus.feature.retrofit.MainViewModel
import com.sms.moLotus.feature.retrofit.MyViewModelFactory
import com.sms.moLotus.feature.retrofit.RetrofitService
import kotlinx.android.synthetic.main.intro_activity_2.*
import kotlinx.android.synthetic.main.intro_activity_main.carrier_provider

class IntroActivity2 : AppCompatActivity() {

    var languages = mutableListOf<String>()

    //var languages = mutableListOf("Telkomsel", "Indosat", "XL Axiata", "Celcom", "U Mobile")
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getInstance()

    private fun getOperators() {
        viewModel.operatorsList.observe(this, { it ->
            Log.e("=====", "response:: $it")
            it.forEach { element ->
                languages.add(element.operator_name)
            }

            Log.e("=====", "languages added:: $languages")

        })
        viewModel.errorMessage.observe(this, {
            Log.e("=====", "errorMessage:: $it")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity_2)

        languages.add(0, "Select carrier provider")
        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )
        getOperators()
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
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                (parent.getChildAt(0) as TextView).textSize = 14f

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
        btnNext.setOnClickListener {
            val CarrierText = carrier_provider.selectedItem.toString()
            val CarrierId = carrier_provider.selectedItemId.toInt()

            if (CarrierText == "Select carrier provider") {
                showToast(message = "Please select carrier provider")
            }

            if (CarrierText != "Select carrier provider") {
                val intent = Intent(this, APNDetailsActivity::class.java);
                intent.putExtra("CarrierText", CarrierText)
                intent.putExtra("CarrierId", CarrierId)
                startActivity(intent)
            }
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

