package com.sms.moLotus.common.base

import androidx.lifecycle.LifecycleOwner

interface QkView<in State> : LifecycleOwner {

    fun render(state: State)

}