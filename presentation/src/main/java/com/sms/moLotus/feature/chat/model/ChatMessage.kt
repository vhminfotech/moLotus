package com.sms.moLotus.feature.chat.model

data class ChatMessage(
    val senderId: String,
    val threadId: String,
    val message: String,
    val dateSent: String,
    val id: String,
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
