package com.kpstv.xclipper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.localized.dao.*
import com.kpstv.xclipper.data.model.*
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.small
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Database(
    entities = [
        Clip::class,
        Tag::class,
        Definition::class,
        UrlInfo::class,
        UserEntity::class,
        Preview::class
    ],
    version = 4,
    exportSchema = false
)

@TypeConverters(
    com.kpstv.xclipper.data.model.ClipListConverter::class,
    com.kpstv.xclipper.data.model.DeviceListConverter::class,
    com.kpstv.xclipper.data.model.UserEntityConverter::class,
    DateConverter::class,
    TagConverter::class
)

abstract class MainDatabase : RoomDatabase() {
    abstract fun clipDataDao(): ClipDataDao
    abstract fun clipTagDao(): TagDao
    abstract fun clipDefineDao(): DefineDao
    abstract fun clipUrlDao(): UrlDao
    abstract fun clipCurrentUserDao(): UserEntityDao
    abstract fun clipLinkPreviewDao(): PreviewDao

    @Singleton
    class RoomCallback @Inject constructor(
        private val database: Provider<MainDatabase>
    ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val clipTagDao = database.get().clipTagDao()

            launchInIO {
                ClipTag.values().forEach {
                    clipTagDao.insert(Tag.from(it.small()))
                }
                clipTagDao.insert(Tag.from("sample tag"))
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "main.db"
    }
}