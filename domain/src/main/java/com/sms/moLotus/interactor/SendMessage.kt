package com.sms.moLotus.interactor

import android.content.Context
import android.os.Handler
import android.util.Log
import com.sms.moLotus.compat.TelephonyCompat
import com.sms.moLotus.extensions.mapNotNull
import com.sms.moLotus.model.Attachment
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import com.sms.moLotus.repository.SyncRepository
import io.reactivex.Flowable
import timber.log.Timber
import timber.log.Timber.d
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge,
    private val syncManager: SyncRepository,
    private val syncMessages: SyncMessages
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
                Log.e("SEND_MESSAGE","params.attachments:: ${params.attachments}")
                Log.e("SEND_MESSAGE","params.addresses:: ${params.addresses}")
                Log.e("SEND_MESSAGE","params.body:: ${params.body}")
                Log.e("SEND_MESSAGE","params.delay:: ${params.delay}")
                Log.e("SEND_MESSAGE","params.subId:: ${params.subId}")
                messageRepo.sendMessage(
                    params.subId, threadId, params.addresses, params.body, params.attachments,
                        params.delay)
            }
            .mapNotNull {
                // If the threadId wasn't provided, then it's probably because it doesn't exist in Realm.
                // Sync it now and get the id
                when (params.threadId) {
                    0L -> conversationRepo.getOrCreateConversation(params.addresses)?.id
                    else -> params.threadId
                }
            }
//            .doOnNext { syncManager.syncMessages() }
        .doOnNext { syncMessages.execute(Unit) }
            .doOnNext { threadId -> conversationRepo.updateConversations(threadId) }
            .doOnNext { threadId -> conversationRepo.markUnarchived(threadId) }
            .flatMap { updateBadge.buildObservable(Unit) } // Update the widget



    /*private fun sendMessage() {
        var msg = thread_type_message.value
        if (msg.isEmpty() && attachmentSelections.isEmpty()) {
            return
        }

        msg = removeDiacriticsIfNeeded(msg)

        val numbers = ArrayList<String>()
        participants.forEach {
            it.phoneNumbers.forEach {
                numbers.add(it.normalizedNumber)
            }
        }

        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = config.enableDeliveryReports

        val SIMId = availableSIMCards.getOrNull(currentSIMCardIndex)?.subscriptionId
        if (SIMId != null) {
            settings.subscriptionId = SIMId
            numbers.forEach {
                config.saveUseSIMIdAtNumber(it, SIMId)
            }
        }

        val transaction = Transaction(this, settings)
        val message = com.klinker.android.send_message.Message(msg, numbers.toTypedArray())

        if (attachmentSelections.isNotEmpty()) {
            for (selection in attachmentSelections.values) {
                try {
                    val byteArray = contentResolver.openInputStream(selection.uri)?.readBytes() ?: continue
                    val mimeType = contentResolver.getType(selection.uri) ?: continue
                    message.addMedia(byteArray, mimeType)
                } catch (e: Exception) {
                    showErrorToast(e)
                } catch (e: Error) {
                    toast(e.localizedMessage ?: getString(R.string.unknown_error_occurred))
                }
            }
        }

        try {
            val smsSentIntent = Intent(this, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(this, SmsStatusDeliveredReceiver::class.java)

            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            refreshedSinceSent = false
            transaction.sendNewMessage(message, threadId)
            thread_type_message.setText("")
            attachmentSelections.clear()
            thread_attachments_holder.beGone()
            thread_attachments_wrapper.removeAllViews()

            Handler().postDelayed({
                if (!refreshedSinceSent) {
                    refreshMessages()
                }
            }, 2000)
        } catch (e: Exception) {
            showErrorToast(e)
        } catch (e: Error) {
            toast(e.localizedMessage ?: getString(R.string.unknown_error_occurred))
        }
    }*/

}
