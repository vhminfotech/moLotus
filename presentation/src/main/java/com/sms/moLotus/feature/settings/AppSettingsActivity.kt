package com.sms.moLotus.feature.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sms.moLotus.PreferenceHelper
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.feature.intro.IntroActivity2
import kotlinx.android.synthetic.main.activity_app_settings.*
import kotlinx.android.synthetic.main.layout_add_signature.*
import kotlinx.android.synthetic.main.layout_header.*

class AppSettingsActivity : AppCompatActivity() {
    var dialog: Dialog? = null
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)
        dialog = Dialog(this)
        imgBack?.setOnClickListener {
            onBackPressed()
        }
        Log.e("SETTINGS", "===" + dialog?.isShowing)
        toggleSignature?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                openSignatureDialog()
            }
        }

        txtAPN?.setOnClickListener {
            PreferenceHelper.setPreference(this,"isSettings",true)
            startActivity(Intent(this, IntroActivity2::class.java))
        }
    }

    private fun openSignatureDialog() {
        //val dialog = Dialog(this)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_add_signature)

        val window: Window? = dialog?.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        wlp?.width = FrameLayout.LayoutParams.MATCH_PARENT
        wlp?.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.attributes = wlp

        Log.e("SETTINGS", "===" + dialog?.isShowing)


        dialog?.txtOk?.setOnClickListener {
            dialog?.dismiss()
            toggleSignature?.isChecked = false
           /* llSignatureText.visibility = View.VISIBLE
            view7.visibility = View.VISIBLE*/
            txtSignatureText?.text = dialog?.etSign?.text.toString()
        }
        dialog?.txtCancel?.setOnClickListener {
            dialog?.dismiss()
            toggleSignature?.isChecked = false
        }


        dialog?.show()
    }
}