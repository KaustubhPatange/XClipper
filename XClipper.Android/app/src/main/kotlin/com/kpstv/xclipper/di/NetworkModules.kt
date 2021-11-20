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
    fun provideDictionaryApi(): GoogleDictionaryApi {
        return RetrofitUtils.getRetrofitBuilder()
            .baseUrl(GoogleDictionaryApi.BASE_URL)
            .client(RetrofitUtils.getHttpBuilder().build())
            .build()
            .create(GoogleDictionaryApi::class.java)
    }

    @[Provides Singleton]
    fun provideTinyUrlApi(): TinyUrlApi {
        return RetrofitUtils.getRetrofitBuilder()
            .baseUrl(TinyUrlApi.BASE_URL)
            .client(RetrofitUtils.getHttpBuilder().build())
            .build()
            .create(TinyUrlApi::class.java)
    }
}