package com.sms.moLotus.feature.settings.swipe

import androidx.annotation.DrawableRes
import com.sms.moLotus.R

data class SwipeActionsState(
    @DrawableRes val rightIcon: Int = R.drawable.ic_archive_white_24dp,
    val rightLabel: String = "",

    @DrawableRes val leftIcon: Int = R.drawable.ic_archive_white_24dp,
    val leftLabel: String = ""
)