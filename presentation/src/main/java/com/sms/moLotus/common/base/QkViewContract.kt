package com.sms.moLotus.common.base

import androidx.lifecycle.LifecycleOwner

interface QkViewContract<in State>: LifecycleOwner {

    fun render(state: State)

}