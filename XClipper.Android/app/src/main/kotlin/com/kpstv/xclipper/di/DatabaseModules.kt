package com.kpstv.xclipper.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.localized.dao.*
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.MainRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
object DatabaseModules {

    @[Provides Singleton]
    fun provideMainDatabase(
        @ApplicationContext context: Context,
        callback: MainDatabase.RoomCallback
    ): MainDatabase =
        Room.databaseBuilder(
            context,
            MainDatabase::class.java,
            MainDatabase.DATABASE_NAME
        )
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .fallbackToDestructiveMigrationFrom(3) // provide db version to destruct it
            .addCallback(callback)
            .build()

    @[Provides Singleton]
    fun provideClipDataDao(mainDatabase: MainDatabase): ClipDataDao = mainDatabase.clipDataDao()

    @[Provides Singleton]
    fun provideClipTagDao(mainDatabase: MainDatabase): TagDao = mainDatabase.clipTagDao()

    @[Provides Singleton]
    fun provideClipDefineDao(mainDatabase: MainDatabase): DefineDao = mainDatabase.clipDefineDao()

    @[Provides Singleton]
    fun provideClipUrlDao(mainDatabase: MainDatabase): UrlDao = mainDatabase.clipUrlDao()

    @[Provides Singleton]
    fun provideUserEntityDao(mainDatabase: MainDatabase): UserEntityDao = mainDatabase.clipCurrentUserDao()

    @[Provides Singleton]
    fun provideLinkPreviewDao(mainDatabase: MainDatabase): PreviewDao = mainDatabase.clipLinkPreviewDao()
}

@[Module InstallIn(SingletonComponent::class)]
object RepositoryModule {

    @[Provides Singleton]
    fun provideMainRepository(
        clipDataDao: ClipDataDao,
        firebaseProvider: FirebaseProvider
    ): MainRepository = MainRepositoryImpl(clipDataDao, firebaseProvider)
}