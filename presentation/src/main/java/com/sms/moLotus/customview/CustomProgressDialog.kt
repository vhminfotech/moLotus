package com.sms.moLotus.customview

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.layout_custom_progress.view.*

class CustomProgressDialog(context: Context) {

    var dialog: CustomDialog =CustomDialog(context)



    fun show(context: Context): Dialog {
        return show(context, null)
    }

    fun hide() {
        dialog.dismiss()
    }


    fun show(context: Context, title: CharSequence?): Dialog {
        val inflater = (context as Activity).layoutInflater
        val view = inflater.inflate(R.layout.layout_custom_progress, null)
        if (title != null) {
            view.cp_title.text = title
        }

        // Card Color
        view.cp_cardview.setCardBackgroundColor(Color.parseColor("#70000000"))

        // Progress Bar Color
        setColorFilter(
            view.cp_pbar.indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.tools_theme, null)
        )

        // Text Color
        view.cp_title.setTextColor(Color.WHITE)

        dialog.setContentView(view)
        dialog.show()
        return dialog
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            // Set Semi-Transparent Color for Dialog Background
            window?.decorView?.rootView?.setBackgroundResource(android.R.color.transparent)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }
}