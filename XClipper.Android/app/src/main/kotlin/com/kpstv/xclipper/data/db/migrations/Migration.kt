package com.kpstv.xclipper.data.db.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.ClipTagType
import com.kpstv.xclipper.extensions.small

object Migration {

    fun getAll(): Array<Migration> = arrayOf(Migration_4_5)

    val Migration_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // gather previous data

            val names = mutableListOf<String>()
            val types = mutableListOf<ClipTagType>()

            val cursor = db.query("SELECT * FROM table_tag")
            val columns: Map</* name */ String, /* index */ Int> = cursor.columnNames.associateWith { cursor.getColumnIndex(it) }

            if (!cursor.moveToFirst()) return
            do {
                columns.forEach { (name, index) ->
                    when(name) {
                        "name" -> names.add(cursor.getString(index))
                    }
                }
            } while(cursor.moveToNext())

            names.removeAll { ClipTag.fromValue(it) != null }
            ClipTag.values().forEach { types.add(if (it.marker == 1) ClipTagType.SPECIAL_TAG else ClipTagType.SYSTEM_TAG) }

            repeat(names.size) { types.add(ClipTagType.USER_TAG) }

            val finalNames = ClipTag.values().map { it.small() }.toList() + names

            // drop the table
            db.execSQL("DROP TABLE table_tag")

            db.execSQL("""
                CREATE TABLE table_tag (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL
                )
            """.trimIndent())

            for(i in finalNames.indices) {
                val contentValues = ContentValues().apply {
                    put("id", i)
                    put("name", finalNames[i])
                    put("type", types[i].name)
                }
                db.insert("table_tag", SQLiteDatabase.CONFLICT_REPLACE, contentValues)
            }
        }
    }
}