package com.sms.moLotus.feature.retrofit

import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.model.APNParamDetails
import com.sms.moLotus.feature.model.Operators
import com.sms.moLotus.feature.model.LoginResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.GsonBuilder

import com.google.gson.Gson


interface RetrofitService {

    @GET("operators")
    fun getAllOperators(): Call<List<Operators>>

    @GET("apn-params/{ID}")
    fun getApnDetails(@Path("ID") id: Int): Call<APNParamDetails>

    @FormUrlEncoded
    @POST("registration")
    fun registerUser(
        @Field("name") name: String,
        @Field("operator") operator: Int,
        @Field("MSISDN") MSISDN: String
    ): Call<LoginResponse>

    companion object {
        var retrofitService: RetrofitService? = null
        var gson = GsonBuilder()
            .setLenient()
            .create()

        fun getInstance(): RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}