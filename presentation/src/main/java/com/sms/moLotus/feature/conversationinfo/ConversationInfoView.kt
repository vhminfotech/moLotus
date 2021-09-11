package com.sms.moLotus.feature.conversationinfo

import com.sms.moLotus.common.base.QkViewContract
import io.reactivex.Observable

interface ConversationInfoView : QkViewContract<ConversationInfoState> {

    fun recipientClicks(): Observable<Long>
    fun recipientLongClicks(): Observable<Long>
    fun themeClicks(): Observable<Long>
    fun nameClicks(): Observable<*>
    fun nameChanges(): Observable<String>
    fun notificationClicks(): Observable<*>
    fun archiveClicks(): Observable<*>
    fun blockClicks(): Observable<*>
    fun deleteClicks(): Observable<*>
    fun confirmDelete(): Observable<*>
    fun mediaClicks(): Observable<Long>

    fun showNameDialog(name: String)
    fun showThemePicker(recipientId: Long)
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun requestDefaultSms()
    fun showDeleteDialog()

}
