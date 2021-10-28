package com.sms.moLotus.interactor

import com.sms.moLotus.manager.NotificationManager
import com.sms.moLotus.repository.ConversationRepository
import com.sms.moLotus.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class DeleteMessages @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val notificationManager: NotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<DeleteMessages.Params>() {

    data class Params(val messageIds: List<Long>, val threadId: Long)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params.messageIds.toLongArray())
                .doOnNext { messageIds -> messageRepo.deleteMessages(*messageIds) } // Delete the messages
                .doOnNext { conversationRepo.updateConversations(params.threadId) } // Update the conversation
                .doOnNext { notificationManager.update(params.threadId) }
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

}