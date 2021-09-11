package com.sms.moLotus.common.widget

import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.text_input_dialog.view.*

class TextInputDialog(context: Activity, hint: String, listener: (String) -> Unit) : AlertDialog(context) {

    private val layout = LayoutInflater.from(context).inflate(R.layout.text_input_dialog, null)

    init {
        layout.field.hint = hint

        setView(layout)
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.button_cancel)) { _, _ -> }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.button_delete)) { _, _ -> listener("") }
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_save)) { _, _ ->
            listener(layout.field.text.toString())
        }
    }

    fun setText(text: String): TextInputDialog {
        layout.field.setText(text)
        return this
    }

}
