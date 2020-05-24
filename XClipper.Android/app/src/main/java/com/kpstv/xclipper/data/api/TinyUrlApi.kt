package com.kpstv.xclipper.data.api

import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TinyUrlApi {
    @GET("/api-create.php")
    fun shortenAsync(@Query("url") longUrl: String): Deferred<Response<ResponseBody>>

    companion object {
        private var tinyUrlApi: TinyUrlApi? = null
        operator fun invoke(retrofitUtils: RetrofitUtils): TinyUrlApi {
            return tinyUrlApi
                ?: retrofitUtils.getRetrofitBuilder()
                    .baseUrl("https://tinyurl.com")
                    .client(retrofitUtils.getHttpBuilder().build())
                    .build()
                    .create(TinyUrlApi::class.java)
                    .also { tinyUrlApi = it }
        }
    }
}