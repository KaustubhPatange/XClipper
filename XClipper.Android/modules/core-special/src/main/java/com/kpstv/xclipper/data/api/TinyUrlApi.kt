package com.kpstv.xclipper.data.api

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TinyUrlApi {
    companion object {
        const val BASE_URL = "https://tinyurl.com"
    }

    @GET("/api-create.php")
    fun shortenAsync(@Query("url") longUrl: String): Deferred<Response<ResponseBody>>
}