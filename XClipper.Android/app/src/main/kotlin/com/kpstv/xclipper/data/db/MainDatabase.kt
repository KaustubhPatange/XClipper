package com.kpstv.xclipper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.xclipper.data.converters.*
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.localized.dao.*
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.data.model.Preview
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
    version = 5,
)

@TypeConverters(
    ClipListConverter::class,
    DeviceListConverter::class,
    UserEntityConverter::class,
    TagConverter::class,
    DateConverter::class,
    ClipTagConverter::class
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
                ClipTag.values().forEach { tag ->
                    val type = if (tag.marker == 1) ClipTagType.SPECIAL_TAG else ClipTagType.SYSTEM_TAG
                    clipTagDao.insert(Tag.from(tag.small(), type))
                }
                clipTagDao.insert(Tag.from("sample tag", ClipTagType.USER_TAG))
            }
        }
    }

    companion object {
        const val DATABASE_NAME = "main.db"
    }
}