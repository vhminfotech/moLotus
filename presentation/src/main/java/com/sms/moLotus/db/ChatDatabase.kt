package com.sms.moLotus.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sms.moLotus.entity.Message


@Database(
    entities = [Message::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ChatDao

    companion object {
        @Volatile
        private var instance: ChatDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ChatDatabase::class.java,
                "chat_database"
            ).build()

    }
}