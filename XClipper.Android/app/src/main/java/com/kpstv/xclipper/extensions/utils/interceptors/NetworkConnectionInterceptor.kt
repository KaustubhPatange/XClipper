package com.kpstv.xclipper.extensions.utils.interceptors

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Made for my project "Moviesy"
 *
 * Now serves as an addon intercept for okHttp to handle No internet connection error.
 */
class NetworkConnectionInterceptor(
    context: Context
) : Interceptor {

    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isOnline())
            throw NoInternetException("Make sure you have an active data connection")
        return chain.proceed(chain.request())
    }

    private fun isOnline(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}

class NoInternetException(message: String) : IOException(message)