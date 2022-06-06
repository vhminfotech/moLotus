package com.sms.moLotus.feature.chat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*data class ChatMessage(
    val id: String,
    val senderId: String,
    val threadId: String,
    val message: String,
    val dateSent: String,
)*/

@Entity(tableName = "ChatMessageTable")
data class ChatMessage(
    @ColumnInfo(name = "userId")
    val userId: String,
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "senderId")
    val senderId: String,
    @ColumnInfo(name = "threadId")
    val threadId: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "dateSent")
    val dateSent: String,
)
/*@Entity(tableName = "MessageTable")
data class ChatMessage(
    val senderId: String,
    val threadId: String,
    val message: String,
    val dateSent: String,
    val messageId: String,
    val userId: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
) {
    companion object {
        const val senderId = "senderId"
        const val threadId = "threadId"
        const val message = "message"
        const val dateSent = "dateSent"
        const val messageId = "messageId"
        const val userId = "userId"
    }
}*/
