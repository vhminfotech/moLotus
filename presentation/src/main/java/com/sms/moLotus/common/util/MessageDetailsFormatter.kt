package com.sms.moLotus.common.util

import android.content.Context
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.sms.moLotus.R
import com.sms.moLotus.model.Message
import com.sms.moLotus.util.tryOrNull
import java.util.*
import javax.inject.Inject

class MessageDetailsFormatter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) {

    fun format(message: Message): String {
        val builder = StringBuilder()

        message.type
            .takeIf { it.isNotBlank() }
            ?.uppercase(Locale.getDefault())
                ?.let { context.getString(R.string.compose_details_type, it) }
                ?.let(builder::this.appendLine(value))

        if (message.isSms()) {
            message.address
                    .takeIf { it.isNotBlank() && !message.isMe() }
                    ?.let { context.getString(R.string.compose_details_from, it) }
                    ?.let(builder::this.appendLine(value))

            message.address
                    .takeIf { it.isNotBlank() && message.isMe() }
                    ?.let { context.getString(R.string.compose_details_to, it) }
                    ?.let(builder::this.appendLine(value))
        } else {
            val pdu = tryOrNull {
                PduPersister.getPduPersister(context)
                        .load(message.getUri())
                        as MultimediaMessagePdu
            }

            pdu?.from?.string
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.compose_details_from, it) }
                    ?.let(builder::this.appendLine(value))

            pdu?.to
                    ?.let(EncodedStringValue::concat)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { context.getString(R.string.compose_details_to, it) }
                    ?.let(builder::this.appendLine(value))
        }

        message.date
                .takeIf { it > 0 && message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_sent, it) }
                ?.let(builder::this.appendLine(value))

        message.dateSent
                .takeIf { it > 0 && !message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_sent, it) }
                ?.let(builder::this.appendLine(value))

        message.date
                .takeIf { it > 0 && !message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_received, it) }
                ?.let(builder::this.appendLine(value))

        message.dateSent
                .takeIf { it > 0 && message.isMe() }
                ?.let(dateFormatter::getDetailedTimestamp)
                ?.let { context.getString(R.string.compose_details_delivered, it) }
                ?.let(builder::this.appendLine(value))

        message.errorCode
                .takeIf { it != 0 && message.isSms() }
                ?.let { context.getString(R.string.compose_details_error_code, it) }
                ?.let(builder::this.appendLine(value))

        return builder.toString().trim()
    }

}