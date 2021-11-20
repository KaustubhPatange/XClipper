package com.kpstv.xclipper.extensions.utils

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kpstv.xclipper.extensions.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitUtils {

    fun getRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder().apply {
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            addConverterFactory(GsonConverterFactory.create(GsonUtils.get()))
            client(getHttpClient())
        }
    }

    fun getHttpBuilder(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            //.addInterceptor(networkConnectionInterceptor)
            // .addInterceptor(loggingInterceptor)  // TODO: Uncomment this interceptor when needed for debugging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
    }

    fun getHttpClient() = getHttpBuilder().build()

    suspend fun fetch(url: String): Result<Response> {
        return try {
            getHttpClient().newCall(Request.Builder().url(url).build()).await()
        } catch (e: UnknownHostException) { // no-internet connection
            Result.failure(e)
        }
    }
}

fun Response.asString() : String? {
    val data = body?.string()
    close()
    return data
}