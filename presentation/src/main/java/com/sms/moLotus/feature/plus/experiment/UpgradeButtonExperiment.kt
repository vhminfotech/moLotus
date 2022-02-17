package com.sms.moLotus.feature.plus.experiment

import android.content.Context
import androidx.annotation.StringRes
import com.sms.moLotus.R
import com.sms.moLotus.experiment.Experiment
import com.sms.moLotus.experiment.Variant
import com.sms.moLotus.manager.AnalyticsManager
import javax.inject.Inject

class UpgradeButtonExperiment @Inject constructor(
    context: Context,
    analytics: AnalyticsManager
) : Experiment<Int>(context, analytics) {

    override val key: String = "Upgrade Button"

    override val variants: List<Variant<Int>> = listOf(
            Variant("variant_a", R.string.qksms_plus_upgrade),
            Variant("variant_b", R.string.qksms_plus_upgrade_b),
            Variant("variant_c", R.string.qksms_plus_upgrade_c),
            Variant("variant_d", R.string.qksms_plus_upgrade_d))

    override val default: Int = R.string.qksms_plus_upgrade

}