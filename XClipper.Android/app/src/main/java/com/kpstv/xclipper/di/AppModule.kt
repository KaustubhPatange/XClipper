package com.kpstv.xclipper.di

import android.content.Context
import com.kpstv.xclipper.data.localized.dao.UserEntityDao
import com.kpstv.xclipper.data.provider.*
import com.kpstv.xclipper.data.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
        dbConnectionProvider: DBConnectionProvider,
        userEntityDao: UserEntityDao
    ): FirebaseProvider =
        FirebaseProviderImpl(context, dbConnectionProvider, userEntityDao)

    @Singleton
    @Provides
    fun provideClipboard(
        @ApplicationContext context: Context,
        mainRepository: MainRepository
    ): ClipboardProvider =
        ClipboardProviderImpl(context, mainRepository)
}