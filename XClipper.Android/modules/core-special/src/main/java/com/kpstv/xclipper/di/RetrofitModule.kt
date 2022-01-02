package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.api.GoogleDictionaryApi
import com.kpstv.xclipper.data.api.TinyUrlApi
import com.kpstv.xclipper.data.converters.DefinitionDeserializer
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.utils.GsonUtils
import com.kpstv.xclipper.extensions.utils.RetrofitUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
object RetrofitModule {
    @[Provides Singleton]
    fun provideDictionaryApi(): GoogleDictionaryApi {
        val customGsonUtils = GsonUtils.get(Definition::class.java to DefinitionDeserializer())
        return RetrofitUtils.getRetrofitBuilder()
            .baseUrl(GoogleDictionaryApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(customGsonUtils))
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