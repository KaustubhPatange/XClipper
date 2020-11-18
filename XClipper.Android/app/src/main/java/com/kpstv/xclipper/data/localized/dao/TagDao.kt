package com.kpstv.xclipper.data.localized.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.xclipper.data.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag)

    @Transaction
    suspend fun insertTag(tag: Tag) {
        val allData = getAllData()

        if (allData.count { it.name == tag.name } <= 0)
            insert(tag)
    }

//    @Query("update table_tag set count = count + 1 where name = :name")
//    suspend fun incrementCount(name: String)
//
//    @Query("update table_tag set count = count - 1 where name = :name")
//    suspend fun decrement(name: String)
//
//    @Transaction
//    suspend fun decrementCount(name: String) {
//        getTag(name)?.let {
//            if (it.count > 0)
//                decrement(name)
//        }
//    }
//
//    @Query("select * from table_tag where name = :name limit 1")
//    suspend fun getTag(name: String): Tag?

    @Delete
    suspend fun delete(tag: Tag)

    @Query("select * from table_tag")
    suspend fun getAllData(): List<Tag>

    @Query("select * from table_tag order by name")
    fun getAllLiveData(): LiveData<List<Tag>>
}