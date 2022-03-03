package com.sms.moLotus.feature.retrofit

class MainRepository constructor(private val retrofitService: RetrofitService) {
    fun getAllOperators() = retrofitService.getAllOperators()
    fun getApnDetails(id: Int) = retrofitService.getApnDetails(id)
    fun registerUser(name: String, operator: Int, MSISDN: String) =
        retrofitService.registerUser(name, operator, MSISDN)
}