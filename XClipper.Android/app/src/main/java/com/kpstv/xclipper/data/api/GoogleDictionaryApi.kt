package com.kpstv.xclipper.data.api

import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GoogleDictionaryApi {
    companion object {
       const val BASE_URL = "https://api.dictionaryapi.dev/"
    }

    @GET("api/v1/entries/{lang}/{word}")
    fun defineAsync(@Path("lang") lang: String, @Path("word") word: String): Deferred<Definition?>?
}