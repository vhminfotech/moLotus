package com.sms.moLotus.interactor

import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class CancelDelayedMessage @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository
) : Interactor<CancelDelayedMessage.Params>() {

    data class Params(val messageId: Long, val threadId: Long)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(Unit)
                .doOnNext { messageRepo.cancelDelayedSms(params.messageId) }
                .doOnNext { messageRepo.deleteMessages(params.messageId) }
                .doOnNext { conversationRepo.updateConversations(params.threadId) } // Update the conversation
    }

}
