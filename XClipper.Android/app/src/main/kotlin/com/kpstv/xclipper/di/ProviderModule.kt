@file:Suppress("unused")

package com.kpstv.xclipper.di

import com.kpstv.xclipper.data.provider.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class ProviderModule {

    @[Binds Singleton]
    abstract fun bindPreferenceProvider(
        preferenceProviderImpl: PreferenceProviderImpl
    ): PreferenceProvider

    @[Binds Singleton]
    abstract fun bindDBConnection(
        dbConnectionProviderImpl: DBConnectionProviderImpl
    ): DBConnectionProvider

    @[Binds Singleton]
    abstract fun bindFirebaseProvider(
        firebaseProviderImpl: FirebaseProviderImpl
    ): FirebaseProvider

    @[Binds Singleton]
    abstract fun bindClipboardProvider(
        clipboardProviderImpl: ClipboardProviderImpl
    ): ClipboardProvider

    @[Binds Singleton]
    abstract fun bindBackupProvider(
        backupProviderImpl: BackupProviderImpl
    ): BackupProvider

    @[Binds Singleton]
    abstract fun bindAccessibilityServiceProvider(
        accessibilityServiceProviderImpl: ClipboardServiceHelperImpl
    ): ClipboardServiceHelper
}