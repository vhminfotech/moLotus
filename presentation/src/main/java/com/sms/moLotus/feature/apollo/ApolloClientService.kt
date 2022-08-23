package com.sms.moLotus.feature.apollo

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.sms.moLotus.feature.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

object ApolloClientService {
    fun setUpApolloClient(authHeader: String): ApolloClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val builder: Request.Builder =
                    original.newBuilder().method(original.method, original.body)
                //builder.header("Authorization", "Bearer  $authHeader")
                builder.header("User-Agent", "Android Apollo Client")
                //Timber.e("Authorization $authHeader")
                chain.proceed(builder.build())
            }
            .build()

        /*val okHttpClient : OkHttpClient by lazy {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            OkHttpClient.Builder()
                .callTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor).build()
        }*/


        Log.e("ApolloClientService", "BASE_GRAPHQL_URL :: ${Constants.BASE_GRAPHQL_URL}")

        // Initial generic client
       /* val genericClient = ApolloClient.builder()
            .serverUrl("dummyUrl")
            .okHttpClient(okHttpClient)
            .build()

        // When you want to change the serverUrl:
        return genericClient.newBuilder()
            .serverUrl(Constants.BASE_GRAPHQL_URL)
            .build()*/

        return ApolloClient.Builder()
            .serverUrl(Constants.BASE_GRAPHQL_URL)
            .okHttpClient(okHttpClient) //ApolloClient with okhttp
            .build()
    }

}