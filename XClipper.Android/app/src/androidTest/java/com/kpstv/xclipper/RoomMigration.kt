package com.kpstv.xclipper

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kpstv.xclipper.data.db.MainDatabase
import com.kpstv.xclipper.data.db.migrations.Migration
import com.kpstv.xclipper.data.model.ClipTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomMigration {
    private val DB_NAME = "test_db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MainDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate4To5() {
        var db = helper.createDatabase(DB_NAME, 4).apply {

            var i = 0
            ClipTag.values().forEachIndexed { index, tag ->
                val values = ContentValues().apply {
                    put("id", index)
                    put("name", tag.name)
                }
                insert("table_tag", SQLiteDatabase.CONFLICT_REPLACE, values)
                i = index
            }
            i++
            val value = ContentValues().apply {
                put("id", i)
                put("name", "sample tag")
            }
            insert("table_tag", SQLiteDatabase.CONFLICT_REPLACE, value)

            close()
        }

        db = helper.runMigrationsAndValidate(DB_NAME, 5, true, Migration.Migration_4_5)
    }
}