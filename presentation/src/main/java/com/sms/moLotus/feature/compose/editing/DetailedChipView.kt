package com.sms.moLotus.feature.compose.editing

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.RelativeLayout
import com.sms.moLotus.R
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.common.util.extensions.setTint
import com.sms.moLotus.injection.appComponent
import com.sms.moLotus.model.Recipient
import kotlinx.android.synthetic.main.contact_chip_detailed.view.*
import javax.inject.Inject

class DetailedChipView(context: Context) : RelativeLayout(context) {

    @Inject lateinit var colors: Colors

    init {
        View.inflate(context, R.layout.contact_chip_detailed, this)
        appComponent.inject(this)

        setOnClickListener { hide() }

        visibility = View.GONE

        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setRecipient(recipient: Recipient) {
        avatar.setRecipient(recipient)
        name.text = recipient.contact?.name?.takeIf { it.isNotBlank() } ?: recipient.address
        info.text = recipient.address

        colors.theme(recipient).let { theme ->
            card.setBackgroundTint(theme.theme)
            name.setTextColor(theme.textPrimary)
            info.setTextColor(theme.textTertiary)
            delete.setTint(theme.textPrimary)
        }
    }

    fun show() {
        startAnimation(AlphaAnimation(0f, 1f).apply { duration = 200 })

        visibility = View.VISIBLE
        requestFocus()
        isClickable = true
    }

    fun hide() {
        startAnimation(AlphaAnimation(1f, 0f).apply { duration = 200 })

        visibility = View.GONE
        clearFocus()
        isClickable = false
    }

    fun setOnDeleteListener(listener: (View) -> Unit) {
        delete.setOnClickListener(listener)
    }

}
