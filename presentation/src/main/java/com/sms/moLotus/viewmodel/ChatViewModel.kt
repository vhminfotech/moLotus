package com.sms.moLotus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.sms.moLotus.DeleteThreadMutation
import com.sms.moLotus.db.ChatDatabase
import com.sms.moLotus.feature.apollo.ApolloClientService
import com.sms.moLotus.feature.chat.model.ChatMessage
import com.sms.moLotus.feature.chat.model.Users
import com.sms.moLotus.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository =
        ChatRepository(ChatDatabase.getDatabase(application.applicationContext))
    val deleteThread = MutableLiveData<DeleteThreadMutation.Data>()
    val errorMessage = MutableLiveData<String>()
    private var client: ApolloClient? = null
    fun insert(message: ChatMessage) = viewModelScope.launch {
        chatRepository.insert(message)
    }

    fun insertAllUsers(users: Users) = viewModelScope.launch {
        chatRepository.insertAllUsers(users)
    }

    fun updateThreadId(id: String, myId: String, userId: String) = viewModelScope.launch {

        //6375fef3bb6cd99baab8922b
        //63727462bb6cd99baab84856
        chatRepository.updateThreadId(id,myId, userId)
    }

    fun insertAllMessages(list: List<ChatMessage>) = viewModelScope.launch {
        chatRepository.insertAllMessages(list)
    }

    fun deleteThread(userId: String, threadId: List<String>) {
        client = ApolloClientService.setUpApolloClient("")
        val deleteThreadMutation = DeleteThreadMutation(threadId, userId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(deleteThreadMutation)?.execute()
                deleteThread.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun getAllChat(id: String) = chatRepository.getAllChat(id)
    fun getAllUsers(id: String) = chatRepository.getAllUsers(id)
    fun clearThreadId(idList: List<String>) = chatRepository.clearThreadId(idList)
    fun deleteMessage(id: List<String>) = chatRepository.deleteMessage(id)
    fun deleteAllMessages(id: List<String>) = chatRepository.deleteAllMessages(id)

    fun deleteTable() = chatRepository.deleteTable()
    fun deleteUsersTable() = chatRepository.deleteUsersTable()

}