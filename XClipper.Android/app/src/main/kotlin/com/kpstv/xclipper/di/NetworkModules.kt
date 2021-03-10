package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
object NetworkModules {

    @[Provides Singleton]
    fun provideDictionaryApi(retrofitUtils: RetrofitUtils): GoogleDictionaryApi {
        return retrofitUtils.getRetrofitBuilder()
            .baseUrl(GoogleDictionaryApi.BASE_URL)
            .client(retrofitUtils.getHttpBuilder().build())
            .build()
            .create(GoogleDictionaryApi::class.java)
    }

    @[Provides Singleton]
    fun provideTinyUrlApi(retrofitUtils: RetrofitUtils): TinyUrlApi {
        return retrofitUtils.getRetrofitBuilder()
            .baseUrl(TinyUrlApi.BASE_URL)
            .client(retrofitUtils.getHttpBuilder().build())
            .build()
            .create(TinyUrlApi::class.java)
    }
}