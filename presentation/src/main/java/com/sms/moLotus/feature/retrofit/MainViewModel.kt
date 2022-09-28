package com.sms.moLotus.feature.retrofit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Upload
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.exoplayer2.util.Log
import com.sms.moLotus.*
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.apollo.ApolloClientService
import com.sms.moLotus.feature.model.MessageList
import com.sms.moLotus.feature.model.Operators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModel : ViewModel() {
    val repository = MainRepository(RetrofitService.getInstance())
    val operatorsList = MutableLiveData<List<Operators>>()
    val versionCode = MutableLiveData<GetAppConfigQuery.GetAppConfig>()
    val apnDetails = MutableLiveData<GetApnDetailsQuery.Data>()
    val userUsingApp = MutableLiveData<GetUserUsingAppQuery.Data>()

    //    val loginResponse = MutableLiveData<LoginResponse>()
    val chatList = MutableLiveData<GetThreadListQuery.Data>()
    val allMessages = MutableLiveData<GetMessageListQuery.Data>()
    val allGroupMessages = MutableLiveData<GetGroupMessageListQuery.Data>()
    val getGroupDetails = MutableLiveData<GetGroupDetailsQuery.Data>()
    val exitGroup = MutableLiveData<ExitGroupMutation.Data>()
    val blockUser = MutableLiveData<BlockUserMutation.Data>()
    val unBlockUser = MutableLiveData<UnBlockUserMutation.Data>()
    val createAdmin = MutableLiveData<CreateUserAAdminOfGroupMutation.Data>()
    val removeAdmin = MutableLiveData<DismissionAdminMutation.Data>()
    val removeParticipant = MutableLiveData<RemoveParticipantFromGroupIfUAreAdminMutation.Data>()
    val uploadAttachments = MutableLiveData<UploadAttachmentsMutation.Data>()
    val sendMessage = MutableLiveData<MessageList>()
    private var client: ApolloClient? = null
    val registerUser = MutableLiveData<RegisterUserMutation.Data>()
    val createThread = MutableLiveData<CreateThreadMutation.Data>()
    val createMessage = MutableLiveData<CreateMessageMutation.Data>()
    val forwardMessage = MutableLiveData<ForwardMessageMutation.Data>()
    val deleteMessage = MutableLiveData<DeleteMessagesMutation.Data>()
    val deleteThread = MutableLiveData<DeleteThreadMutation.Data>()
    val otpMessage = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    fun getOTP(msisdn: String) {
        val response = repository.getOtp("Hello!",msisdn)
        response.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                android.util.Log.e("=====", "success:: $response")

                otpMessage.postValue(response.body())
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

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
        Log.e("========================", "getChatList = $getChatList")

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getChatList)?.execute()
                Log.e("========================", "response = $response")
                chatList.postValue(response?.data)
            } catch (e: Exception) {
                Log.e("========================", "error = ${e.message}")

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

    fun getAllGroupMessages(threadId: String, senderId: String, token: String) {
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
        recipientsIds: ArrayList<String>?,
        token: String,
        isGroup: Boolean,
        groupName: String,
        url: String,
    ) {
        client = ApolloClientService.setUpApolloClient(token)
        val createThreadMutation =
            CreateThreadMutation(message, userId, recipientsIds!!, isGroup, groupName, url)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(createThreadMutation)?.execute()
                createThread.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }

    }

    fun createMessage(
        message: String,
        threadId: String,
        senderId: String,
        token: String,
        url: String,
        receiverId: String,
    ) {
        client = ApolloClientService.setUpApolloClient(token)
        val createMessageMutation =
            CreateMessageMutation(message, threadId, senderId, receiverId, url)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(createMessageMutation)?.execute()
                createMessage.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun forwardMessage(
        message: String,
        threadId: String,
        senderId: String,
        receiverId: String,
        url: String,
    ) {
        client = ApolloClientService.setUpApolloClient("")
        val forwardMessageMutation =
            ForwardMessageMutation(message, threadId, senderId, receiverId, url)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(forwardMessageMutation)?.execute()
                forwardMessage.postValue(response?.data)
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

    fun getGroupDetails(groupId: String, token: String) {
        client = ApolloClientService.setUpApolloClient("")
        val getGroupDetailsQuery = GetGroupDetailsQuery(groupId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.query(getGroupDetailsQuery)?.execute()
                getGroupDetails.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun exitGroup(groupId: String, myUserID: String) {
        client = ApolloClientService.setUpApolloClient("")
        val exitGroupMutation = ExitGroupMutation(groupId, myUserID)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(exitGroupMutation)?.execute()
                exitGroup.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun blockUser(myUserId: String, userToBlockId: String) {
        client = ApolloClientService.setUpApolloClient("")
        val blockUserMutation = BlockUserMutation(myUserId, userToBlockId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(blockUserMutation)?.execute()
                blockUser.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun unBlockUser(myUserId: String, userToUnblockId: String) {
        client = ApolloClientService.setUpApolloClient("")
        val unBlockUserMutation = UnBlockUserMutation(myUserId, userToUnblockId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(unBlockUserMutation)?.execute()
                unBlockUser.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun createAdmin(groupId: String, userId: String) {
        client = ApolloClientService.setUpApolloClient("")
        val createAdminMutation = CreateUserAAdminOfGroupMutation(groupId, userId)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(createAdminMutation)?.execute()
                createAdmin.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun removeAdmin(groupId: String, userId: String, userToBeDismissID: String) {
        client = ApolloClientService.setUpApolloClient("")
        val removeAdminMutation = DismissionAdminMutation(groupId, userId, userToBeDismissID)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(removeAdminMutation)?.execute()
                removeAdmin.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun removeParticipant(groupId: String, userId: String, userToBeRemovedID: String) {
        client = ApolloClientService.setUpApolloClient("")
        val removeParticipantMutation =
            RemoveParticipantFromGroupIfUAreAdminMutation(groupId, userId, userToBeRemovedID)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(removeParticipantMutation)?.execute()
                removeParticipant.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }

    fun uploadAttachments(upload: Upload) {
        client = ApolloClientService.setUpApolloClient("")
        val uploadAttachmentsMutation = UploadAttachmentsMutation(upload)

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = client?.mutation(uploadAttachmentsMutation)?.execute()
                uploadAttachments.postValue(response?.data)
            } catch (e: ApolloException) {
                errorMessage.postValue(e.message)
            }
        }
    }
}