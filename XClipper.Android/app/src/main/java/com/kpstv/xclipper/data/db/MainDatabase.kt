package com.kpstv.xclipper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip

@Database(
    entities = [
        Clip::class
    ],
    version = 1
)

@TypeConverters(
    DateConverter::class,
    TagConverter::class
)

abstract class MainDatabase: RoomDatabase() {
    abstract fun clipMainDao(): ClipDataDao

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
              //  .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}