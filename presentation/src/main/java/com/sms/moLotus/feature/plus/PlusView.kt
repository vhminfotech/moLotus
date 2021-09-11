package com.sms.moLotus.feature.plus

import com.sms.moLotus.common.base.QkView
import com.sms.moLotus.manager.BillingManager
import io.reactivex.Observable

interface PlusView : QkView<PlusState> {

    val upgradeIntent: Observable<Unit>
    val upgradeDonateIntent: Observable<Unit>
    val donateIntent: Observable<*>
    val themeClicks: Observable<*>
    val scheduleClicks: Observable<*>
    val backupClicks: Observable<*>
    val delayedClicks: Observable<*>
    val nightClicks: Observable<*>

    fun initiatePurchaseFlow(billingManager: BillingManager, sku: String)

}