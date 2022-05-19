package com.sms.moLotus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.entity.Message
import com.sms.moLotus.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository =
        ChatRepository(ChatDatabase(application.applicationContext))
    fun insert(message: Message) = viewModelScope.launch {
        chatRepository.insert(message)
    }
    fun getAllChat(id: String) = chatRepository.getAllChat(id)

}