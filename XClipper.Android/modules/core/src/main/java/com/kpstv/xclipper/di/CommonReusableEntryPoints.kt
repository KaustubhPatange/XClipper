package com.kpstv.xclipper.di

import android.content.Context
import com.kpstv.xclipper.data.provider.ClipboardServiceHelper
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.helpers.AppSettings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CommonReusableEntryPoints {
    fun appSettings() : AppSettings
    fun preferenceProvider() : PreferenceProvider
    fun dbConnectionProvider() : DBConnectionProvider
    fun clipboardServiceHelper() : ClipboardServiceHelper
    fun firebaseProvider() : FirebaseProvider

    companion object {
        fun get(applicationContext: Context) : CommonReusableEntryPoints = EntryPointAccessors.fromApplication(applicationContext, CommonReusableEntryPoints::class.java)
    }
}