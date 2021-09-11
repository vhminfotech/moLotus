package com.sms.moLotus.feature.blocking

import com.sms.moLotus.common.base.QkViewContract
import io.reactivex.Observable

interface BlockingView : QkViewContract<BlockingState> {

    val blockingManagerIntent: Observable<*>
    val blockedNumbersIntent: Observable<*>
    val blockedMessagesIntent: Observable<*>
    val dropClickedIntent: Observable<*>

    fun openBlockingManager()
    fun openBlockedNumbers()
    fun openBlockedMessages()
}
