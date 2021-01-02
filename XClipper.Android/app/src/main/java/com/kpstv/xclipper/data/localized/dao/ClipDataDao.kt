package com.kpstv.xclipper.data.localized.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.kpstv.xclipper.data.model.Clip
import java.util.*

@Dao
interface ClipDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: Clip)

    @Update
    suspend fun update(clip: Clip)

    @Delete
    suspend fun delete(clip: Clip)

    @Query("delete from table_clip where id = :id")
    suspend fun delete(id: Int)

    @Query("delete from table_clip where data = :data")
    suspend fun delete(data: String): Int

    @Query("delete from table_clip where id = (select MIN(id) from table_clip);")
    suspend fun deleteFirst()

    @Query("select * from table_clip where id = :id")
    suspend fun getData(id: Int): Clip?

    @Query("select * from table_clip where data = :data")
    suspend fun getData(data: String): Clip?

    @Query("select exists(select data from table_clip where data = :data)")
    suspend fun isExist(data: String): Boolean

    @Query("select * from table_clip")
    suspend fun getAllData(): List<Clip>?

    @Query("select count(*) from table_clip")
    suspend fun getClipSize(): Long

    @Query("update table_clip set isPinned = :isPinned where id = :id")
    suspend fun updatePin(id: Int, isPinned: Boolean): Int

    /** We are reversing this result in CIAdapter that is why we are taking
     *  list in ascending order */
    @Query("select * from table_clip order by isPinned")
    fun getAllLiveData(): LiveData<List<Clip>>

    /** As compared to above we are not reversing the list during submission,
     *  so we've to apply descending order filter */
    @Query("select * from table_clip order by isPinned desc")
    fun getDataSource(): DataSource.Factory<Int, Clip>
}