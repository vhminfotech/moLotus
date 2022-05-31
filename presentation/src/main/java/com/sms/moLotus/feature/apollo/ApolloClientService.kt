package com.sms.moLotus.feature.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.sms.moLotus.feature.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

object ApolloClientService {
    fun setUpApolloClient(authHeader: String): ApolloClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val builder: Request.Builder =
                    original.newBuilder().method(original.method, original.body)
                builder.header("Authorization","Bearer  $authHeader")
                Timber.e("Authorization $authHeader")
                chain.proceed(builder.build())
            }
            .build()

        return ApolloClient.builder()
            .serverUrl(Constants.BASE_GRAPHQL_URL)
            .okHttpClient(okHttpClient) //ApolloClient with okhttp
            .build()
    }
}