package com.sms.moLotus.common.util

import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.widget.EditText
import android.widget.TextView
import com.sms.moLotus.R
import com.sms.moLotus.common.util.TextViewStyler.Companion.SIZE_PRIMARY
import com.sms.moLotus.common.util.TextViewStyler.Companion.SIZE_SECONDARY
import com.sms.moLotus.common.util.TextViewStyler.Companion.SIZE_TERTIARY
import com.sms.moLotus.common.util.TextViewStyler.Companion.SIZE_TOOLBAR
import com.sms.moLotus.common.util.extensions.getColorCompat
import com.sms.moLotus.common.widget.QkEditText
import com.sms.moLotus.common.widget.QkTextView
import com.sms.moLotus.util.Preferences
import javax.inject.Inject



class TextViewStyler @Inject constructor(
    private val prefs: Preferences,
    private val colors: Colors,
    private val fontProvider: FontProvider
) {

    companion object {
        const val COLOR_THEME = 0
        const val COLOR_PRIMARY_ON_THEME = 1
        const val COLOR_SECONDARY_ON_THEME = 2
        const val COLOR_TERTIARY_ON_THEME = 3

        const val SIZE_PRIMARY = 0
        const val SIZE_SECONDARY = 1
        const val SIZE_TERTIARY = 2
        const val SIZE_TOOLBAR = 3
        const val SIZE_DIALOG = 4
        const val SIZE_EMOJI = 5

        fun applyEditModeAttributes(textView: TextView, attrs: AttributeSet?) {
            textView.run {
                var colorAttr = 0
                var textSizeAttr = 0

                when (this) {
                    is QkTextView -> context.obtainStyledAttributes(attrs, R.styleable.QkTextView)?.run {
                        colorAttr = getInt(R.styleable.QkTextView_textColor, -1)
                        textSizeAttr = getInt(R.styleable.QkTextView_textSize, -1)
                        recycle()
                    }

                    is QkEditText -> context.obtainStyledAttributes(attrs, R.styleable.QkEditText)?.run {
                        colorAttr = getInt(R.styleable.QkEditText_textColor, -1)
                        textSizeAttr = getInt(R.styleable.QkEditText_textSize, -1)
                        recycle()
                    }

                    else -> return
                }
                setTextColor(when (colorAttr) {
                    COLOR_PRIMARY_ON_THEME -> context.getColorCompat(R.color.textPrimaryDark)
                    COLOR_SECONDARY_ON_THEME -> context.getColorCompat(R.color.textSecondaryDark)
                    COLOR_TERTIARY_ON_THEME -> context.getColorCompat(R.color.textTertiaryDark)
                    COLOR_THEME -> context.getColorCompat(R.color.tools_theme)
                    else -> currentTextColor
                })

                textSize = when (textSizeAttr) {
                    SIZE_PRIMARY -> 16f
                    SIZE_SECONDARY -> 14f
                    SIZE_TERTIARY -> 12f
                    SIZE_TOOLBAR -> 20f
                    SIZE_DIALOG -> 18f
                    SIZE_EMOJI -> 32f
                    else -> textSize / paint.density
                }
            }
        }
    }

    fun applyAttributes(textView: TextView, attrs: AttributeSet?) {
        var colorAttr = 0
        var textSizeAttr = 0

        if (!prefs.systemFont.get()) {
            fontProvider.getLato { lato ->
                textView.setTypeface(lato, textView.typeface?.style ?: Typeface.NORMAL)
            }
        }

        when (textView) {
            is QkTextView -> textView.context.obtainStyledAttributes(attrs, R.styleable.QkTextView).run {
                colorAttr = getInt(R.styleable.QkTextView_textColor, -1)
                textSizeAttr = getInt(R.styleable.QkTextView_textSize, -1)
                recycle()
            }

            is QkEditText -> textView.context.obtainStyledAttributes(attrs, R.styleable.QkEditText).run {
                colorAttr = getInt(R.styleable.QkEditText_textColor, -1)
                textSizeAttr = getInt(R.styleable.QkEditText_textSize, -1)
                recycle()
            }

            else -> return
        }

        when (colorAttr) {
            COLOR_THEME -> textView.setTextColor(colors.theme().theme)
            COLOR_PRIMARY_ON_THEME -> textView.setTextColor(colors.theme().textPrimary)
            COLOR_SECONDARY_ON_THEME -> textView.setTextColor(colors.theme().textSecondary)
            COLOR_TERTIARY_ON_THEME -> textView.setTextColor(colors.theme().textTertiary)
        }

        setTextSize(textView, textSizeAttr)

        if (textView is EditText) {
            val drawable = textView.resources.getDrawable(R.drawable.cursor).apply { setTint(colors.theme().theme) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textView.textCursorDrawable = drawable
            }
        }
    }

    /**
     * @see SIZE_PRIMARY
     * @see SIZE_SECONDARY
     * @see SIZE_TERTIARY
     * @see SIZE_TOOLBAR
     */
    fun setTextSize(textView: TextView, textSizeAttr: Int) {
        val textSizePref = prefs.textSize.get()
        when (textSizeAttr) {
            SIZE_PRIMARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 14f
                Preferences.TEXT_SIZE_NORMAL -> 16f
                Preferences.TEXT_SIZE_LARGE -> 18f
                Preferences.TEXT_SIZE_LARGER -> 20f
                else -> 16f
            }

            SIZE_SECONDARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 12f
                Preferences.TEXT_SIZE_NORMAL -> 14f
                Preferences.TEXT_SIZE_LARGE -> 16f
                Preferences.TEXT_SIZE_LARGER -> 18f
                else -> 14f
            }

            SIZE_TERTIARY -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 10f
                Preferences.TEXT_SIZE_NORMAL -> 12f
                Preferences.TEXT_SIZE_LARGE -> 14f
                Preferences.TEXT_SIZE_LARGER -> 16f
                else -> 12f
            }

            SIZE_TOOLBAR -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 18f
                Preferences.TEXT_SIZE_NORMAL -> 20f
                Preferences.TEXT_SIZE_LARGE -> 22f
                Preferences.TEXT_SIZE_LARGER -> 26f
                else -> 20f
            }

            SIZE_DIALOG -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 16f
                Preferences.TEXT_SIZE_NORMAL -> 18f
                Preferences.TEXT_SIZE_LARGE -> 20f
                Preferences.TEXT_SIZE_LARGER -> 24f
                else -> 18f
            }

            SIZE_EMOJI -> textView.textSize = when (textSizePref) {
                Preferences.TEXT_SIZE_SMALL -> 28f
                Preferences.TEXT_SIZE_NORMAL -> 32f
                Preferences.TEXT_SIZE_LARGE -> 36f
                Preferences.TEXT_SIZE_LARGER -> 40f
                else -> 32f
            }
        }
    }

}