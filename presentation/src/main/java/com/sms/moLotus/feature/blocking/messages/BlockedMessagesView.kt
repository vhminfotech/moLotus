package com.sms.moLotus.feature.blocking.messages

import com.sms.moLotus.common.base.QkViewContract
import io.reactivex.Observable

interface BlockedMessagesView : QkViewContract<BlockedMessagesState> {

    val menuReadyIntent: Observable<Unit>
    val optionsItemIntent: Observable<Int>
    val conversationClicks: Observable<Long>
    val selectionChanges: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>
    val backClicked: Observable<*>

    fun clearSelection()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun goBack()

}
