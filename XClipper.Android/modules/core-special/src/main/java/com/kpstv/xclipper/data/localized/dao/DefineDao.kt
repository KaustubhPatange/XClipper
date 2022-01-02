package com.kpstv.xclipper.data.localized.dao

import androidx.room.*
import com.kpstv.xclipper.data.model.Definition

@Dao
interface DefineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(definition: Definition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(definitions: List<Definition>)

    @Transaction
    suspend fun insertDefinition(definition: Definition) {
        if (definition.word != null)
            insert(definition)
    }

    @Query("select * from table_define where word = :word")
    suspend fun getWord(word: String): Definition?

    @Query("select * from table_define")
    suspend fun getAllWords(): List<Definition>

    @Query("delete from table_define")
    suspend fun deleteAll()
}