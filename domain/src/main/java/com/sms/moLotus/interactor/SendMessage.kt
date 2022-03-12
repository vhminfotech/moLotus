package com.sms.moLotus.interactor

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.sms.moLotus.compat.TelephonyCompat
import com.sms.moLotus.domain.R
import com.sms.moLotus.extensions.mapNotNull
import com.sms.moLotus.model.Attachment
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import com.sms.moLotus.repository.SyncRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge,
    private val syncManager: SyncRepository,
    private val syncMessages: SyncMessages,
    private val syncMessage: SyncMessage
) : Interactor<SendMessage.Params>() {

    data class Params(
        val subId: Int,
        val threadId: Long,
        val addresses: List<String>,
        val body: String,
        val attachments: List<Attachment> = listOf(),
        val delay: Int = 0
    )

    override fun buildObservable(params: Params): Flowable<*> = Flowable.just(Unit)
        .filter { params.addresses.isNotEmpty() }
        .doOnNext {
            // If a threadId isn't provided, try to obtain one
            val threadId = when (params.threadId) {
                0L -> TelephonyCompat.getOrCreateThreadId(context, params.addresses.toSet())
                else -> params.threadId
            }
            Log.e("SEND_MESSAGE", "params.attachments:: ${params.attachments}")
            Log.e("SEND_MESSAGE", "params.addresses:: ${params.addresses}")
            Log.e("SEND_MESSAGE", "params.body:: ${params.body}")
            Log.e("SEND_MESSAGE", "params.delay:: ${params.delay}")
            Log.e("SEND_MESSAGE", "params.subId:: ${params.subId}")
            messageRepo.sendMessage(
                params.subId, threadId, params.addresses, params.body, params.attachments,
                params.delay
            )
        }
        .mapNotNull {
            // If the threadId wasn't provided, then it's probably because it doesn't exist in Realm.
            // Sync it now and get the id
            when (params.threadId) {
                0L -> conversationRepo.getOrCreateConversation(params.addresses)?.id
                else -> params.threadId
            }
        }
//      .doOnNext { syncManager.syncMessages() }
//      .doOnNext { syncManager.syncNewMessages() }
        .doOnNext { syncMessages.execute(Unit) }
        .doOnNext { threadId -> conversationRepo.updateConversations(threadId) }
        .doOnNext { threadId -> conversationRepo.markUnarchived(threadId) }
        .flatMap { updateBadge.buildObservable(Unit) } // Update the widget


}
