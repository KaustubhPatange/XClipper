package com.kpstv.xclipper.data.api

import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GoogleDictionaryApi {

    @GET("api/v1/entries/{lang}/{word}")
    fun defineAsync(@Path("lang") lang: String, @Path("word") word: String): Deferred<Definition?>?

    companion object {
        private var googleDictionaryApi: GoogleDictionaryApi? = null
        operator fun invoke(retrofitUtils: RetrofitUtils): GoogleDictionaryApi {
            return googleDictionaryApi
                ?: retrofitUtils.getRetrofitBuilder()
                    .baseUrl("https://api.dictionaryapi.dev/")
                    .client(retrofitUtils.getHttpBuilder().build())
                    .build()
                    .create(GoogleDictionaryApi::class.java)
                    .also { googleDictionaryApi = it }
        }
    }
}