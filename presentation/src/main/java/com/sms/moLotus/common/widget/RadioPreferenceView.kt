package com.sms.moLotus.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sms.moLotus.R
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.forwardTouches
import com.sms.moLotus.common.util.extensions.resolveThemeAttribute
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setVisible
import com.sms.moLotus.injection.appComponent
import kotlinx.android.synthetic.main.radio_preference_view.view.*
import javax.inject.Inject

class RadioPreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    @Inject lateinit var colors: Colors

    var title: String? = null
        set(value) {
            field = value

            if (isInEditMode) {
                findViewById<TextView>(R.id.titleView).text = value
            } else {
                titleView.text = value
            }
        }

    var summary: String? = null
        set(value) {
            field = value


            if (isInEditMode) {
                findViewById<TextView>(R.id.summaryView).run {
                    text = value
                    setVisible(value?.isNotEmpty() == true)
                }
            } else {
                summaryView.text = value
                summaryView.setVisible(value?.isNotEmpty() == true)
            }
        }

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.radio_preference_view, this)
        setBackgroundResource(context.resolveThemeAttribute(R.attr.selectableItemBackground))

        val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked))

        val themeColor = when (isInEditMode) {
            true -> context.resources.getColor(R.color.tools_theme)
            false -> colors.theme().theme
        }
        val textSecondary = context.resolveThemeColor(android.R.attr.textColorTertiary)
        radioButton.buttonTintList = ColorStateList(states, intArrayOf(themeColor, textSecondary))
        radioButton.forwardTouches(this)

        context.obtainStyledAttributes(attrs, R.styleable.RadioPreferenceView).run {
            title = getString(R.styleable.RadioPreferenceView_title)
            summary = getString(R.styleable.RadioPreferenceView_summary)

            // If there's a custom view used for the preference's widget, inflate it
            getResourceId(R.styleable.RadioPreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                View.inflate(context, id, widgetFrame)
            }

            recycle()
        }
    }

}