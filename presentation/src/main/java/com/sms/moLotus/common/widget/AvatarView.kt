package com.sms.moLotus.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.sms.moLotus.R
import com.sms.moLotus.common.Navigator
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.setBackgroundTint
import com.sms.moLotus.common.util.extensions.setTint
import com.sms.moLotus.injection.appComponent
import com.sms.moLotus.model.Recipient
import com.sms.moLotus.util.GlideApp
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var navigator: Navigator

    private var lookupKey: String? = null
    private var fullName: String? = null
    private var photoUri: String? = null
    private var lastUpdated: Long? = null
    private var theme: Colors.Theme

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        theme = colors.theme()

        View.inflate(context, R.layout.avatar_view, this)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * Use the [contact] information to display the avatar.
     */
    fun setRecipient(recipient: Recipient?) {
        lookupKey = recipient?.contact?.lookupKey
        fullName = recipient?.contact?.name
        photoUri = recipient?.contact?.photoUri
        lastUpdated = recipient?.contact?.lastUpdate
        theme = colors.theme(recipient)
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        // Apply theme
        setBackgroundTint(theme.theme)
        initial.setTextColor(theme.textPrimary)
        icon.setTint(theme.textPrimary)

        val initials = fullName
                ?.substringBefore(',')
                ?.split(" ").orEmpty()
                .filter { name -> name.isNotEmpty() }
                .map { name -> name[0] }
                .filter { initial -> initial.isLetterOrDigit() }
                .map { initial -> initial.toString() }

        if (initials.isNotEmpty()) {
            initial.text = if (initials.size > 1) initials.first() + initials.last() else initials.first()
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        photoUri?.let { photoUri ->
            GlideApp.with(photo)
                    .load(photoUri)
                    .into(photo)
        }
    }
}
