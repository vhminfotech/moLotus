package com.sms.moLotus.feature.settings.swipe

import com.sms.moLotus.common.base.QkViewContract
import io.reactivex.Observable

interface SwipeActionsView : QkViewContract<SwipeActionsState> {

    enum class Action { LEFT, RIGHT }

    fun actionClicks(): Observable<Action>
    fun actionSelected(): Observable<Int>

    fun showSwipeActions(selected: Int)

}