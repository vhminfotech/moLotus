package com.sms.moLotus.feature.retrofit

import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.model.APNParamDetails
import com.sms.moLotus.feature.model.Operators
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitService {

    @GET("operators")
    fun getAllOperators(): Call<List<Operators>>

    @GET("apn-params/{ID}")
    fun getApnDetails(@Path("ID") id: Int): Call<APNParamDetails>

    companion object {
        var retrofitService: RetrofitService? = null

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}