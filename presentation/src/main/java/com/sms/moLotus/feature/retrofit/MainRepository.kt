package com.sms.moLotus.feature.retrofit

class MainRepository constructor(private val retrofitService: RetrofitService) {
    fun getAllOperators() = retrofitService.getAllOperators()
    fun getVersionCode() = retrofitService.getVersionCode()
    fun getApnDetails(id: Int) = retrofitService.getApnDetails(id)
    fun registerUser(name: String, operator: Int, MSISDN: String) =
        retrofitService.registerUser(name, operator, MSISDN)
    fun getChatList(token: String) = retrofitService.getChatList(token)
    fun getAllMessages(id: Int, token: String) = retrofitService.getAllMessages(id, token)

}