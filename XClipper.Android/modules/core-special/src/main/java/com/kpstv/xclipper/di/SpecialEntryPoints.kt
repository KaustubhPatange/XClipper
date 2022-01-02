package com.kpstv.xclipper.di

import android.content.Context
import com.kpstv.xclipper.data.localized.dao.PreviewDao
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SpecialEntryPoints {
    fun dictionaryApiHelper() : DictionaryApiHelper
    fun linkPreviewDao() : PreviewDao

    companion object {
        fun get(applicationContext: Context) : SpecialEntryPoints = EntryPointAccessors.fromApplication(applicationContext, SpecialEntryPoints::class.java)
    }
}