package com.kpstv.xclipper.utils

import com.kpstv.xclipper.extensions.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

internal data class LinkData(val title: String?, val description: String?, val imageUrl: String?)

internal object LinkUtils {
    suspend fun fetchUrl(url: String) : LinkData? = withContext(Dispatchers.IO) call@{
        try {
            val result = OkHttpClient().newCall(Request.Builder().url(url).build()).await()
            result.onSuccess { response ->
                val body = response.body?.string() ?: return@onSuccess
                response.close()

                var title = matchPattern(body, TITLE_PATTERN)
                if (title == null) title = matchPattern(body, TITLE_PATTERN_FALLBACK)
                val description = matchPattern(body, DESCRIPTION_PATTERN)
                val imageUrl = matchPattern(body, IMAGE_PATTERN)

                return@call LinkData(
                    title = title,
                    description = description,
                    imageUrl = imageUrl
                )
            }
        } catch (e: IOException) { /* no-op */ }
        return@call null
    }

    private const val TITLE_PATTERN = "property=\"og:title\".*?content=\"(.*?)\""
    private const val TITLE_PATTERN_FALLBACK = "<title>(.*?)<\\/title>"
    private const val DESCRIPTION_PATTERN = "property=\"og:description\".*?content=\"(.*?)\""
    private const val IMAGE_PATTERN = "property=\"og:image\".*?content=\"(.*?)\""

    private fun matchPattern(data: String, pattern: String) : String? {
        return pattern.toRegex().find(data)?.groupValues?.get(1)
    }
}