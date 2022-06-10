package com.sms.moLotus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository =
        ChatRepository(ChatDatabase.getDatabase(application.applicationContext))

    fun insert(message: ChatMessage) = viewModelScope.launch {
        chatRepository.insert(message)
    }

    fun insertAllMessages(list: List<ChatMessage>) = viewModelScope.launch {
        chatRepository.insertAllMessages(list)
    }

    fun getAllChat(id: String) = chatRepository.getAllChat(id)
    fun deleteMessage(id: List<String>) = chatRepository.deleteMessage(id)

    fun deleteTable() = chatRepository.deleteTable()

}