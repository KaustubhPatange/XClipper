package com.kpstv.xclipper.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.localized.dao.*
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.MainRepositoryImpl
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModules {

    @Singleton
    @Provides
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
            .fallbackToDestructiveMigrationFrom(2) // provide db version to destruct it
            .addCallback(callback)
            .build()

    @Singleton
    @Provides
    fun provideClipDataDao(mainDatabase: MainDatabase): ClipDataDao = mainDatabase.clipDataDao()

    @Singleton
    @Provides
    fun provideClipTagDao(mainDatabase: MainDatabase): TagDao = mainDatabase.clipTagDao()

    @Singleton
    @Provides
    fun provideClipDefineDao(mainDatabase: MainDatabase): DefineDao = mainDatabase.clipDefineDao()

    @Singleton
    @Provides
    fun provideClipUrlDao(mainDatabase: MainDatabase): UrlDao = mainDatabase.clipUrlDao()

    @Singleton
    @Provides
    fun provideUserEntityDao(mainDatabase: MainDatabase): UserEntityDao = mainDatabase.clipCurrentUserDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideMainRepository(
        clipDataDao: ClipDataDao,
        firebaseProvider: FirebaseProvider
    ): MainRepository = MainRepositoryImpl(clipDataDao, firebaseProvider)
}