package com.kpstv.xclipper.di

import android.content.Context
import com.kpstv.xclipper.data.provider.*
import com.kpstv.xclipper.data.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
@ExperimentalStdlibApi
object AppModule {

    @Singleton
    @Provides
    fun providePreference(@ApplicationContext context: Context): PreferenceProvider =
        PreferenceProviderImpl(context)

    @Singleton
    @Provides
    fun provideDBConnection(preferenceProvider: PreferenceProvider): DBConnectionProvider =
        DBConnectionProviderImpl(preferenceProvider)

    @Singleton
    @Provides
    fun provideFirebase(
        @ApplicationContext context: Context,
        dbConnectionProvider: DBConnectionProvider
    ): FirebaseProvider =
        FirebaseProviderImpl(context, dbConnectionProvider)

    @Singleton
    @Provides
    fun provideClipboard(
        @ApplicationContext context: Context,
        mainRepository: MainRepository
    ): ClipboardProvider =
        ClipboardProviderImpl(context, mainRepository)
}