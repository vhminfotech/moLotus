package com.sms.moLotus.feature.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.activity_app_settings.*

class AppSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        toggleSignature?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                openSignatureDialog()
            }
        }
    }

    private fun openSignatureDialog() {
        val dialog = Dialog(this)
//        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_add_signature)

        val window: Window? = dialog.window
        val wlp: WindowManager.LayoutParams? = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        wlp?.width = FrameLayout.LayoutParams.MATCH_PARENT
        wlp?.height = FrameLayout.LayoutParams.WRAP_CONTENT
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.attributes = wlp

        dialog.show()
    }
}