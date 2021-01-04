package com.kpstv.xclipper.ui.helpers

import android.content.Context
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.data.localized.dao.UrlDao
import com.kpstv.xclipper.data.model.UrlInfo
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TinyUrlApiHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tinyUrlApi: TinyUrlApi,
    private val urlRepository: UrlDao
) {
    suspend fun createShortenUrl(longUrl: String): ResponseResult<UrlInfo> {
        try {
            val data = urlRepository.getUrlInfo(longUrl)
            if (data == null) {
                val response = tinyUrlApi.shortenAsync(longUrl).await()
                return if (response.isSuccessful) {
                    val urlInfo = UrlInfo.from(longUrl, response.body()?.string()!!)
                    urlRepository.insert(urlInfo)
                    ResponseResult.complete(urlInfo)
                } else {
                    ResponseResult.error(context.getString(R.string.error_shorten))
                }
            }
            return ResponseResult.complete(data)
        } catch (e: Exception) {
            return ResponseResult.error(e)
        }
    }
}