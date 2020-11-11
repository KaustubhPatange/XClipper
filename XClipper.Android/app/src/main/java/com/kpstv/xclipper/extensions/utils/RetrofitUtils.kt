package com.kpstv.xclipper.extensions.utils

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.extensions.utils.interceptors.NetworkConnectionInterceptor
import okhttp3.OkHttpClient
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
    private val interceptor: NetworkConnectionInterceptor
) {
    private var retrofitBuilder: Retrofit.Builder? = null
    private var httpBuilder: OkHttpClient.Builder? = null

    fun getRetrofitBuilder(): Retrofit.Builder {
        return retrofitBuilder ?: Retrofit.Builder().apply {
            addCallAdapterFactory(CoroutineCallAdapterFactory())
            addConverterFactory(
                GsonConverterFactory.create(gson)
            )
            client(getHttpClient())
        }.also { retrofitBuilder = it }
    }

    fun getHttpBuilder(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return httpBuilder
            ?: OkHttpClient.Builder()
                .addInterceptor(interceptor)
                // .addInterceptor(loggingInterceptor)  // TODO: Uncomment this interceptor when needed for debugging
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .also { httpBuilder = it }
    }

    fun getHttpClient() = getHttpBuilder().build()

}