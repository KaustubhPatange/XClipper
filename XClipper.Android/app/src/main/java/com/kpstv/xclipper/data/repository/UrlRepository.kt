package com.kpstv.xclipper.data.repository

import com.kpstv.xclipper.data.model.UrlInfo

interface UrlRepository {
    fun insert(urlInfo: UrlInfo)
    suspend fun getData(longUrl: String): UrlInfo?
}