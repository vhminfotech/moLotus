package com.sms.moLotus.feature.retrofit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sms.moLotus.feature.model.APNParamDetails
import com.sms.moLotus.feature.model.LoginResponse
import com.sms.moLotus.feature.model.Operators
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel constructor(private val repository: MainRepository) : ViewModel() {
    val operatorsList = MutableLiveData<List<Operators>>()
    val apnDetails = MutableLiveData<APNParamDetails>()
    val loginResponse = MutableLiveData<LoginResponse>()
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

    fun getApnDetails(id: Int) {
        val response = repository.getApnDetails(id)
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
        })
    }

    fun registerUser(name: String, operator: Int, MSISDN: String) {
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
    }
}