package com.sms.moLotus.feature.chat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UsersTable")
data class Users(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "myUserId")
    val myUserId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "userId")
    val userId: String,
    @ColumnInfo(name = "msisdn")
    val msisdn: String,
    @ColumnInfo(name = "operator")
    val operator: String,
    @ColumnInfo(name = "threadId")
    val threadId: String
)
