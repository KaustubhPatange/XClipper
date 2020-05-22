package com.kpstv.xclipper.data.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.xclipper.data.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tag: Tag)

    @Delete
    fun delete(tag: Tag)

    @Query("select * from table_tag")
    fun getAllData(): List<Tag>

    @Query("select * from table_tag order by name")
    fun getAllLiveData(): LiveData<List<Tag>>
}