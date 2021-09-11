package com.sms.moLotus.feature.settings.about

import com.sms.moLotus.common.base.QkViewContract
import com.sms.moLotus.common.widget.PreferenceView
import io.reactivex.Observable

interface AboutView : QkViewContract<Unit> {

    fun preferenceClicks(): Observable<PreferenceView>

}