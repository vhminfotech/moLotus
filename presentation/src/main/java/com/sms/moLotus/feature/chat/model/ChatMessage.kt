package com.sms.moLotus.feature.chat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
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
    var userId: String,
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "senderId")
    var senderId: String,
    @ColumnInfo(name = "threadId")
    var threadId: String,
    @ColumnInfo(name = "message")
    var message: String,
    @ColumnInfo(name = "dateSent")
    var dateSent: String,
    @ColumnInfo(name = "userName")
    var userName: String,
    @ColumnInfo(name = "url")
    var url: String,
    @ColumnInfo(name = "read")
    var read: Boolean,
){
    @Transient
    @Ignore
    var selected = false
}
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
