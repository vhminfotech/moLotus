package com.sms.moLotus.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sms.moLotus.feature.chat.model.ChatMessage


@Database(
    entities = [ChatMessage::class],
    version = 1, exportSchema = true)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun getChatDao(): ChatDao

    companion object {

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): ChatDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ChatDatabase::class.java,
                "chat_database"
            ).allowMainThreadQueries()
                .build()
        }
    }
}
/* @Volatile
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
     ).build()*/


