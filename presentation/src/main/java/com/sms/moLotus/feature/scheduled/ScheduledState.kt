package com.sms.moLotus.feature.scheduled

import com.sms.moLotus.model.ScheduledMessage
import io.realm.RealmResults

data class ScheduledState(
    val scheduledMessages: RealmResults<ScheduledMessage>? = null,
    val upgraded: Boolean = false
)
