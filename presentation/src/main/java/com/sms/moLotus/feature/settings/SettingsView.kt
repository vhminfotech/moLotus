package com.sms.moLotus.feature.settings

import com.sms.moLotus.common.base.QkViewContract
import com.sms.moLotus.common.widget.PreferenceView
import io.reactivex.Observable

interface SettingsView : QkViewContract<SettingsState> {
    fun preferenceClicks(): Observable<PreferenceView>
    fun aboutLongClicks(): Observable<*>
    fun viewQksmsPlusClicks(): Observable<*>
    fun nightModeSelected(): Observable<Int>
    fun nightStartSelected(): Observable<Pair<Int, Int>>
    fun nightEndSelected(): Observable<Pair<Int, Int>>
    fun textSizeSelected(): Observable<Int>
    fun sendDelaySelected(): Observable<Int>
    fun signatureChanged(): Observable<String>
    fun autoDeleteChanged(): Observable<Int>
    fun mmsSizeSelected(): Observable<Int>

    fun showQksmsPlusSnackbar()
    fun showNightModeDialog()
    fun showStartTimePicker(hour: Int, minute: Int)
    fun showEndTimePicker(hour: Int, minute: Int)
    fun showTextSizePicker()
    fun showDelayDurationDialog()
    fun showSignatureDialog(signature: String)
    fun showAutoDeleteDialog(days: Int)
    suspend fun showAutoDeleteWarningDialog(messages: Int): Boolean
    fun showMmsSizePicker()
    fun showSwipeActions()
    fun showThemePicker()
    fun showAbout()
}
