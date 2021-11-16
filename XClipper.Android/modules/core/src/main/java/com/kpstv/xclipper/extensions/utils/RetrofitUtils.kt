package com.kpstv.xclipper.extensions.utils

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kpstv.xclipper.extensions.await
import com.kpstv.xclipper.extensions.interceptors.NetworkConnectionInterceptor
import com.kpstv.xclipper.extensions.interceptors.NoInternetException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A class made for my project "Moviesy" https://github.com/KaustubhPatange/Moviesy
 *
 * Now serves as a general purpose for managing retrofit builder and
 * http logging utils.
 */
@Singleton
class RetrofitUtils @Inject constructor(
    private val networkConnectionInterceptor: NetworkConnectionInterceptor
) {
    private var retrofitBuilder: Retrofit.Builder? = null
    private var httpBuilder: OkHttpClient.Builder? = null

    fun getRetrofitBuilder(): Retrofit.Builder {
        return retrofitBuilder ?: Retrofit.Builder().apply {
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            addConverterFactory(GsonConverterFactory.create(GsonUtils.get()))
            client(getHttpClient())
        }.also { retrofitBuilder = it }
    }

    fun getHttpBuilder(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return httpBuilder
            ?: OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                // .addInterceptor(loggingInterceptor)  // TODO: Uncomment this interceptor when needed for debugging
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .also { httpBuilder = it }
    }

    fun getHttpClient() = getHttpBuilder().build()

    suspend fun fetch(url: String): Result<Response> {
        return try {
            getHttpClient().newCall(Request.Builder().url(url).build()).await()
        } catch (e: NoInternetException) {
            Result.failure(e)
        }
    }
}

fun Response.asString() : String? {
    val data = body?.string()
    close()
    return data
}