package com.kpstv.xclipper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Tag
import java.util.*
import java.util.concurrent.Executors

@Database(
    entities = [
        Clip::class,
        Tag::class
    ],
    version = 1
)

@TypeConverters(
    DateConverter::class,
    TagConverter::class
)

abstract class MainDatabase : RoomDatabase() {
    abstract fun clipMainDao(): ClipDataDao
    abstract fun clipTagDao(): TagDao

    companion object {
        @Volatile
        private var instance: MainDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
                instance ?: buildDatabase(
                    context
                ).also { instance = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                MainDatabase::class.java,
                "main.db"
            )
                .addCallback(roomCallback)
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadExecutor().execute {
                    instance?.let { database ->
                        with(database.clipTagDao()) {
                            ClipTag.values().forEach {
                                insert(Tag.from(it.name.toLowerCase(Locale.ROOT)))
                            }
                        }
                    }
                }
            }
        }


        /* End of companion object */
    }

}