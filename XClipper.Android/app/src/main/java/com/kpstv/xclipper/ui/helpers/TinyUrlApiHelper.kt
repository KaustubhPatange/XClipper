package com.kpstv.xclipper.ui.helpers

import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.data.model.UrlInfo
import com.kpstv.xclipper.data.repository.UrlRepository
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.mainThread

class TinyUrlApiHelper(
    private val tinyUrlApi: TinyUrlApi,
    private val urlRepository: UrlRepository
) {
    fun createShortenUrl(longUrl: String, responseListener: ResponseListener<UrlInfo>) {
        ioThread {
            try {
                val data = urlRepository.getData(longUrl)
                if (data == null) {
                    val response = tinyUrlApi.shortenAsync(longUrl).await()
                    if (response.isSuccessful) {
                        val urlInfo = UrlInfo.from(longUrl, response.body()?.string()!!)
                        urlRepository.insert(urlInfo)
                        mainThread { responseListener.onComplete(urlInfo) }
                    } else
                        mainThread { responseListener.onError(Exception("Requested url cannot be shorten")) }
                }else {
                    mainThread { responseListener.onComplete(data) }
                }
            } catch (e: Exception) {
                mainThread { responseListener.onError(e) }
            }
        }

    }
}