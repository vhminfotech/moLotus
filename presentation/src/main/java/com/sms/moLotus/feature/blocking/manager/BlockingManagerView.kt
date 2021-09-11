package com.sms.moLotus.feature.blocking.manager

import com.sms.moLotus.common.base.QkViewContract
import io.reactivex.Observable
import io.reactivex.Single

interface BlockingManagerView : QkViewContract<BlockingManagerState> {

    fun activityResumed(): Observable<*>
    fun qksmsClicked(): Observable<*>
    fun callControlClicked(): Observable<*>
    fun siaClicked(): Observable<*>

    fun showCopyDialog(manager: String): Single<Boolean>

}
