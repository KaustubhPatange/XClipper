package com.kpstv.xclipper.extensions.utils

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kpstv.xclipper.extensions.Logger
import com.kpstv.xclipper.extensions.await
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitUtils {

    object CacheStrategy {
        fun cache24Hours() : CacheControl = CacheControl.Builder()
            .maxAge(1, TimeUnit.DAYS)
            .build()
        fun noCache() : CacheControl = CacheControl.Builder().noCache().build()
    }

    fun getRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder().apply {
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            client(getHttpClient())
        }
    }

    fun getHttpBuilder(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            //.addInterceptor(networkConnectionInterceptor)
            // .addInterceptor(loggingInterceptor)  // Uncomment this interceptor when needed for debugging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
    }

    fun getHttpClient() = getHttpBuilder().build()

    suspend fun fetch(url: String, cacheControl: CacheControl = CacheStrategy.noCache()): Result<Response> {
        return try {
            getHttpClient().newCall(
                Request.Builder().cacheControl(cacheControl).url(url).build()
            ).await()
        } catch (e: UnknownHostException) { // no-internet connection
            Result.failure(e)
        }
    }
}

fun Response.asString() : String? {
    return try {
        val data = body?.string()
        close()
        data
    } catch (e: IOException) {
        Logger.w(e, "Error: Failed to convert response.asString()")
        null
    }
}