package com.sms.moLotus.customview

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import kotlinx.android.synthetic.main.intro_activity_main.*

object CustomStringBuilder {

    fun mChatBuilder(txtMchat: TextView) {
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
    }
}