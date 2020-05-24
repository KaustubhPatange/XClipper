package com.kpstv.xclipper.ui.helpers

import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import java.lang.Exception

class TinyUrlApiHelper(
    private val tinyUrlApi: TinyUrlApi
) {
    fun createShortenUrl(longUrl: String, responseListener: ResponseListener<UrlInfo>) {
        Coroutines.main {
            try {
                val response = tinyUrlApi.shortenAsync(longUrl).await()
                if (response.isSuccessful)
                    responseListener.onComplete(
                        UrlInfo(longUrl, response.body()?.string()!!)
                    )
                else
                    responseListener.onError(Exception("Requested url cannot be shorten"))
            }catch (e: Exception) {
                responseListener.onError(e)
            }

        }
    }
}

data class UrlInfo(
    val longUrl: String,
    val shortUrl: String
)