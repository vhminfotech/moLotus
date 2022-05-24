package com.sms.moLotus.feature.retrofit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.sms.moLotus.GetApnDetailsQuery
import com.sms.moLotus.RegisterUserMutation
import com.sms.moLotus.feature.apollo.ApolloClientService
import com.sms.moLotus.feature.model.ChatList
import com.sms.moLotus.feature.model.MessageList
import com.sms.moLotus.feature.model.Operators
import com.sms.moLotus.feature.model.VersionCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel constructor(private val repository: MainRepository) : ViewModel() {
    val operatorsList = MutableLiveData<List<Operators>>()
    val versionCode = MutableLiveData<List<VersionCode>>()
    val apnDetails = MutableLiveData<GetApnDetailsQuery.Data>()

    //    val loginResponse = MutableLiveData<LoginResponse>()
    val chatList = MutableLiveData<ArrayList<ChatList>>()
    val allMessages = MutableLiveData<MessageList>()
    val sendMessage = MutableLiveData<MessageList>()
    private var client: ApolloClient? = null
    val registerUser = MutableLiveData<RegisterUserMutation.Data>()
    val errorMessage = MutableLiveData<String>()

    fun getAllOperators() {
        val response = repository.getAllOperators()
        response.enqueue(object : Callback<List<Operators>> {
            override fun onResponse(
                call: Call<List<Operators>>,
                response: Response<List<Operators>>
            ) {
                operatorsList.postValue(response.body())
            }

            override fun onFailure(call: Call<List<Operators>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getVersionCode() {
        val response = repository.getVersionCode()
        response.enqueue(object : Callback<List<VersionCode>> {
            override fun onResponse(
                call: Call<List<VersionCode>>,
                response: Response<List<VersionCode>>
            ) {
                versionCode.postValue(response.body())
            }

            override fun onFailure(call: Call<List<VersionCode>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getApnDetails(id: Int) {
        client = ApolloClientService.setUpApolloClient("")
        val getAPNParamDetails = GetApnDetailsQuery(id.toString())
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val response = client?.query(getAPNParamDetails)?.execute()
                apnDetails.postValue(response?.data)
            }
        } catch (e: ApolloException) {
            errorMessage.postValue(e.message)
        }
        /*val response = repository.getApnDetails(id)
        response.enqueue(object : Callback<APNParamDetails> {
            override fun onResponse(
                call: Call<APNParamDetails>,
                response: Response<APNParamDetails>
            ) {
                apnDetails.postValue(response.body())
            }

            override fun onFailure(call: Call<APNParamDetails>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })*/
    }

    /* fun registerUser(name: String, operator: Int, MSISDN: String) {
         val response = repository.registerUser(name, operator, MSISDN)
         response.enqueue(object : Callback<LoginResponse> {
             override fun onResponse(
                 call: Call<LoginResponse>,
                 response: Response<LoginResponse>
             ) {
                 loginResponse.postValue(response.body())
             }

             override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                 errorMessage.postValue(t.message)
             }
         })
     }*/

    fun getChatList(token: String) {
        val response = repository.getChatList(token)
        response.enqueue(object : Callback<ArrayList<ChatList>> {
            override fun onResponse(
                call: Call<ArrayList<ChatList>>,
                response: Response<ArrayList<ChatList>>
            ) {
                chatList.postValue(response.body())
            }

            override fun onFailure(call: Call<ArrayList<ChatList>>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun getAllMessages(threadId: Int, token: String) {
        val response = repository.getAllMessages(threadId, token)
        response.enqueue(object : Callback<MessageList> {
            override fun onResponse(
                call: Call<MessageList>,
                response: Response<MessageList>
            ) {
                allMessages.postValue(response.body())
            }

            override fun onFailure(call: Call<MessageList>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun sendMessage(user_id: ArrayList<Int>, text: String, threadId: Int, token: String) {
        val response = repository.sendMessage(user_id, text, threadId, token)
        response.enqueue(object : Callback<MessageList> {
            override fun onResponse(
                call: Call<MessageList>,
                response: Response<MessageList>
            ) {
                sendMessage.postValue(response.body())
            }

            override fun onFailure(call: Call<MessageList>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun registerUser(name: String, operator: String, MSISDN: String) {
        client = ApolloClientService.setUpApolloClient("")
        val registerUserMutation = RegisterUserMutation(name, operator, MSISDN)
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val response = client?.mutation(registerUserMutation)?.execute()
                registerUser.postValue(response?.data)
            }
        } catch (e: ApolloException) {
            errorMessage.postValue(e.message)
        }
    }
}