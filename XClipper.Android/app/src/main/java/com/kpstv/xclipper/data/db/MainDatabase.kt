package com.kpstv.xclipper.data.db

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.localized.dao.ClipDataDao
import com.kpstv.xclipper.data.localized.dao.DefineDao
import com.kpstv.xclipper.data.localized.dao.TagDao
import com.kpstv.xclipper.data.localized.dao.UrlDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.small
import com.kpstv.xclipper.data.model.UrlInfo
import com.kpstv.xclipper.extensions.Coroutines
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Database(
    entities = [
        Clip::class,
        Tag::class,
        Definition::class,
        UrlInfo::class
    ],
    version = 1
)

@TypeConverters(
    DateConverter::class,
    TagConverter::class
)

abstract class MainDatabase : RoomDatabase() {
    abstract fun clipDataDao(): ClipDataDao
    abstract fun clipTagDao(): TagDao
    abstract fun clipDefineDao(): DefineDao
    abstract fun clipUrlDao(): UrlDao

    @Singleton
    class RoomCallback @Inject constructor(
        private val database: Provider<MainDatabase>
    ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val clipTagDao = database.get().clipTagDao()

            Coroutines.io {
                ClipTag.values().forEach {
                    clipTagDao.insert(Tag.from(it.small()))
                }
                clipTagDao.insert(Tag.from("sample tag"))
                Log.e("RoomCallback", "Injecting sample data")
            }
        }
    }
}