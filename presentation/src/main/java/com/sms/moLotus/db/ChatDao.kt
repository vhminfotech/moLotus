package com.sms.moLotus.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sms.moLotus.entity.Message

@Dao
interface ChatDao {


    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM MessageTable WHERE idUser LIKE :id ORDER BY id DESC")
    fun getAllChat(id: String): LiveData<List<Message>>


}