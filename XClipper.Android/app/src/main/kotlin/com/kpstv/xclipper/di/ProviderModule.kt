package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.provider.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Singleton
    @Binds
    abstract fun bindPreferenceProvider(
        preferenceProviderImpl: PreferenceProviderImpl
    ): PreferenceProvider

    @Singleton
    @Binds
    abstract fun bindDBConnection(
        dbConnectionProviderImpl: DBConnectionProviderImpl
    ): DBConnectionProvider

    @Singleton
    @Binds
    abstract fun bindFirebaseProvider(
        firebaseProviderImpl: FirebaseProviderImpl
    ): FirebaseProvider

    @Singleton
    @Binds
    abstract fun bindClipboardProvider(
        clipboardProviderImpl: ClipboardProviderImpl
    ): ClipboardProvider

    @Singleton
    @Binds
    abstract fun bindBackupProvider(
        backupProviderImpl: BackupProviderImpl
    ): BackupProvider
}