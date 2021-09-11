package com.sms.moLotus.feature.settings.autodelete

import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.settings_auto_delete_dialog.view.*

class AutoDeleteDialog(context: Activity, listener: (Int) -> Unit) : AlertDialog(context) {

    private val layout = LayoutInflater.from(context).inflate(R.layout.settings_auto_delete_dialog, null)

    init {
        setView(layout)
        setTitle(R.string.settings_auto_delete)
        setMessage(context.getString(R.string.settings_auto_delete_dialog_message))
        setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.button_cancel)) { _, _ -> }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.settings_auto_delete_never)) { _, _ -> listener(0) }
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_save)) { _, _ ->
            listener(layout.field.text.toString().toIntOrNull() ?: 0)
        }
    }

    fun setExpiry(days: Int): AutoDeleteDialog {
        when (days) {
            0 -> layout.field.text = null
            else -> layout.field.setText(days.toString())
        }
        return this
    }

}
