package com.sms.moLotus.feature.qkreply

import com.sms.moLotus.compat.SubscriptionInfoCompat
import com.sms.moLotus.model.Conversation
import com.sms.moLotus.model.Message
import io.realm.RealmResults

data class QkReplyState(
    val hasError: Boolean = false,
    val threadId: Long = 0,
    val title: String = "",
    val expanded: Boolean = false,
    val data: Pair<Conversation, RealmResults<Message>>? = null,
    val remaining: String = "",
    val subscription: SubscriptionInfoCompat? = null,
    val canSend: Boolean = false
)