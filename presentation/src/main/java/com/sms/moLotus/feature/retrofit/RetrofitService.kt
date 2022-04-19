package com.sms.moLotus.feature.retrofit

import com.google.gson.GsonBuilder
import com.sms.moLotus.feature.Constants
import com.sms.moLotus.feature.model.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


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

    @GET("app_config/1")
    fun getVersionCode(): Call<List<VersionCode>>

    @Headers("Accept: application/json")
    @GET("threads")
    fun getChatList(@Header("Authorization") token: String): Call<ArrayList<ChatList>>

    @Headers("Accept: application/json")
    @GET("thread/{ID}")
    fun getAllMessages(
        @Path("ID") id: Int,
        @Header("Authorization") token: String
    ): Call<MessageList>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("compose/{ID}")
    fun sendMessage(
        @Field("user_id") user_id: ArrayList<Int>,
        @Field("text") text: String,
        @Path("ID") id: Int,
        @Header("Authorization") token: String
    ): Call<MessageList>

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