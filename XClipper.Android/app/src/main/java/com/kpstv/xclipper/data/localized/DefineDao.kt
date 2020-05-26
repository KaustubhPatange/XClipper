package com.kpstv.xclipper.data.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.xclipper.data.model.Definition

@Dao
interface DefineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(definition: Definition)

    @Query("select * from table_define where word = :word")
    fun getWord(word: String): Definition?
}