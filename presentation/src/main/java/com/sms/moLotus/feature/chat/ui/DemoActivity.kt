package com.sms.moLotus.feature.chat.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.sms.moLotus.R
import kotlinx.android.synthetic.main.activity_demo.*
import kotlinx.android.synthetic.main.dialog_send_attachments.*

class DemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        btnOpen?.setOnClickListener {
            openFolder()
        }
    }

    private fun openFolder() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Complete action using"),
            100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val data = data?.data
            Log.e("============", "data:::: $data")
            showSendAttachmentDialog()

        }
    }


    private fun showSendAttachmentDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_send_attachments)

        dialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btnSend.setOnClickListener {

            dialog.dismiss()
        }
        dialog.show()

    }
}