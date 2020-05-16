package com.kpstv.xclipper.data.localized

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kpstv.xclipper.data.model.Clip

@Dao
interface ClipDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(clip: Clip)

    @Update
    fun update(clip: Clip)

    @Delete
    fun delete(clip: Clip)

    @Query("delete from table_clip where id = :id")
    fun delete(id: Int)

    @Query("select * from table_clip")
    fun getAllData(): List<Clip>

    @Query("select * from table_clip")
    fun getAllLiveData(): LiveData<List<Clip>>
}