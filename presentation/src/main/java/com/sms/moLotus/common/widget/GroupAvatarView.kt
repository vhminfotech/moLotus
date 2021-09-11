package com.sms.moLotus.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.sms.moLotus.R
import com.sms.moLotus.common.util.extensions.getColorCompat
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.model.Recipient
import kotlinx.android.synthetic.main.group_avatar_view.view.*

class GroupAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var recipients: List<Recipient> = ArrayList()
        set(value) {
            field = value.sortedWith(compareByDescending { contact -> contact.contact?.lookupKey })
            updateView()
        }

    init {
        View.inflate(context, R.layout.group_avatar_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatar1Frame.setBackgroundTint(when (recipients.size > 1) {
            true -> context.resolveThemeColor(android.R.attr.windowBackground)
            false -> context.getColorCompat(android.R.color.transparent)
        })
        avatar1Frame.updateLayoutParams<LayoutParams> {
            matchConstraintPercentWidth = if (recipients.size > 1) 0.75f else 1.0f
        }
        avatar2.isVisible = recipients.size > 1


        recipients.getOrNull(0).run(avatar1::setRecipient)
        recipients.getOrNull(1).run(avatar2::setRecipient)
    }

}
