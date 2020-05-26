package com.kpstv.xclipper.data.repository

import com.kpstv.xclipper.data.localized.UrlDao
import com.kpstv.xclipper.extensions.ioThread
import com.kpstv.xclipper.data.model.UrlInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UrlRepositoryImpl(
    private val urlDao: UrlDao
) : UrlRepository {
    override fun insert(urlInfo: UrlInfo) {
        ioThread {
            urlDao.insert(urlInfo)
        }
    }

    override suspend fun getData(longUrl: String) =
        withContext(Dispatchers.IO) {
            urlDao.getUrlInfo(longUrl)
        }
}