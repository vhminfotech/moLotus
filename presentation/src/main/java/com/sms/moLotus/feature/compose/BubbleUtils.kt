package com.sms.moLotus.feature.compose

import com.sms.moLotus.R
import com.sms.moLotus.model.Message
import java.util.concurrent.TimeUnit

object BubbleUtils {

    const val TIMESTAMP_THRESHOLD = 10

    fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
    }

    fun getBubble(emojiOnly: Boolean, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean, isMe: Boolean): Int {
        return when {
            emojiOnly -> R.drawable.message_emoji
            !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_first else R.drawable.message_in_first
            canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_middle else R.drawable.message_in_middle
            canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_out_last else R.drawable.message_in_last
            else -> R.drawable.message_only
        }
    }

}