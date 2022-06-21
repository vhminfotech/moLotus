package com.sms.moLotus.feature.retrofit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.sms.moLotus.*
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.apollo.ApolloClientService
import com.sms.moLotus.feature.model.MessageList
import com.sms.moLotus.feature.model.Operators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainViewModel constructor(/*private val repository: MainRepository*/) : ViewModel() {
    val operatorsList = MutableLiveData<List<Operators>>()
    val versionCode = MutableLiveData<GetAppConfigQuery.GetAppConfig>()
    val apnDetails = MutableLiveData<GetApnDetailsQuery.Data>()
    val userUsingApp = MutableLiveData<GetUserUsingAppQuery.Data>()

    //    val loginResponse = MutableLiveData<LoginResponse>()
    val chatList = MutableLiveData<GetThreadListQuery.Data>()
    val allMessages = MutableLiveData<GetMessageListQuery.Data>()
    val allGroupMessages = MutableLiveData<GetGroupMessageListQuery.Data>()
    val sendMessage = MutableLiveData<MessageList>()
    private var client: ApolloClient? = null
    val registerUser = MutableLiveData<RegisterUserMutation.Data>()
    val createThread = MutableLiveData<CreateThreadMutation.Data>()
    val createMessage = MutableLiveData<CreateMessageMutation.Data>()
    val deleteMessage = MutableLiveData<DeleteMessagesMutation.Data>()
    val deleteThread = MutableLiveData<DeleteThreadMutation.Data>()
    val errorMessage = MutableLiveData<String>()

    fun getAllOperators() {
        /*  val response = repository.getAllOperators()
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
          })*/
    }

    fun getVersionCode() {
        client = ApolloClientService.setUpApolloClient("")
        val getAppConfig = GetAppConfigQuery(Constants.CARRIER_ID.toString())

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getAppConfig)?.execute()
                if (response?.data?.getAppConfig != null) {
                    versionCode.postValue(response.data?.getAppConfig)
                } else {
                    errorMessage.postValue("null")
                }
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun getApnDetails(id: Int) {
        client = ApolloClientService.setUpApolloClient("")
        val getAPNParamDetails = GetApnDetailsQuery(id.toString())

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getAPNParamDetails)?.execute()
                apnDetails.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun getUserUsingAppList(userId: String, contactList: List<String>) {
        client = ApolloClientService.setUpApolloClient("")
        val getUserUsingApp = GetUserUsingAppQuery(userId, contactList)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getUserUsingApp)?.execute()
                userUsingApp.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

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

    fun getChatList(userId: String, token: String) {
        client = ApolloClientService.setUpApolloClient("")
        val getChatList = GetThreadListQuery(userId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getChatList)?.execute()
                chatList.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun getAllMessages(threadId: String, receiverId: String, senderId: String, token: String) {
        client = ApolloClientService.setUpApolloClient("")
        val getAllMessages = GetMessageListQuery(threadId, senderId, receiverId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getAllMessages)?.execute()
                allMessages.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun getAllGroupMessages(threadId: String,  senderId: String, token: String) {
        client = ApolloClientService.setUpApolloClient("")
        val getAllGroupMessages = GetGroupMessageListQuery(threadId, senderId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getAllGroupMessages)?.execute()
                allGroupMessages.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun registerUser(name: String, operator: String, MSISDN: String) {
        client = ApolloClientService.setUpApolloClient("")
        val registerUserMutation = RegisterUserMutation(name, operator, MSISDN)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(registerUserMutation)?.execute()
                registerUser.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun createThread(
        message: String,
        userId: String,
        recipientsIds: ArrayList<String>,
        token: String,
        isGroup: Boolean,
        groupName : String
    ) {
        client = ApolloClientService.setUpApolloClient(token)
        val createThreadMutation = CreateThreadMutation(message, userId, recipientsIds, isGroup, groupName)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(createThreadMutation)?.execute()
                createThread.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun createMessage(message: String, threadId: String, senderId: String, token: String) {
        client = ApolloClientService.setUpApolloClient(token)
        val createMessageMutation = CreateMessageMutation(message, threadId, senderId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(createMessageMutation)?.execute()
                createMessage.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun deleteMessage(threadId: String, userId: String, messageId: List<String>) {
        client = ApolloClientService.setUpApolloClient("")
        val deleteMessageMutation = DeleteMessagesMutation(threadId, userId, messageId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(deleteMessageMutation)?.execute()
                deleteMessage.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

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
}