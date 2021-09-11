package com.sms.moLotus.feature.blocking.messages

import com.sms.moLotus.model.Conversation
import io.realm.RealmResults

data class BlockedMessagesState(
    val data: RealmResults<Conversation>? = null,
    val selected: Int = 0
)
