package com.kpstv.xclipper.data.localized.dao

import androidx.room.*
import com.kpstv.xclipper.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tags: List<Tag>)

    @Transaction
    suspend fun insertTag(tag: Tag) {
        val allData = getAllData()

        if (allData.count { it.name == tag.name } <= 0)
            insert(tag)
    }

    @Delete
    suspend fun delete(tag: Tag)

    @Query("delete from table_tag")
    suspend fun deleteAll()

    @Query("select * from table_tag")
    suspend fun getAllData(): List<Tag>

    @Query("select * from table_tag order by name")
    fun getAllLiveData(): Flow<List<Tag>>
}