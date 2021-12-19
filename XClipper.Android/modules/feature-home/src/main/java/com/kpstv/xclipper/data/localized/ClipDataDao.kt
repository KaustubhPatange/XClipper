package com.kpstv.xclipper.data.localized

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.PartialClipTagMap
import com.kpstv.xclipper.data.model.Tag
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ClipDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: Clip)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clips: List<Clip>)

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

    @Query("delete from table_clip")
    suspend fun deleteAll()

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
    @Query("select * from table_clip order by isPinned desc, time desc")
    fun getAllLiveData(): LiveData<List<Clip>>

    /** As compared to above we are not reversing the list during submission,
     *  so we've to apply descending order filter */
    @Query("select * from table_clip where data like :wildcard order by isPinned desc, time desc")
    fun getDataSource(wildcard: String): DataSource.Factory<Int, Clip>

    @Query("select id, tags from table_clip where tags != '{}'")
    fun getAllTags(): Flow<List<PartialClipTagMap>>

    @Query("select count(id) from table_clip")
    fun getTotalCount(): LiveData<Int>

    @RawQuery(observedEntities = [Clip::class])
    fun getData(query: SupportSQLiteQuery): List<Clip>

    // TODO: Unused method
    @RawQuery(observedEntities = [Clip::class])
    fun getObservableDataSource(query: SupportSQLiteQuery): DataSource.Factory<Int, Clip>

    companion object {
        fun createQuery(searchFilter: ArrayList<String>?, tagFilter: ArrayList<Tag>?, searchText: String?): SimpleSQLiteQuery {
            val builder = StringBuilder("select * from table_clip")
            val params = mutableListOf<Any>()
            if (searchText?.isNotEmpty() == true || searchFilter?.size ?: 0 > 0 || tagFilter?.size ?: 0 > 0) builder.append(" where ")
            if (searchText != null && searchText.isNotEmpty()) {
                params.add("%$searchText%")
                builder.append("data like ? and ")
            }
            searchFilter?.forEach { filter ->
                params.add("%$filter%")
                builder.append("data like ? and ")
            }
            tagFilter?.map { it.name }?.forEach { filter ->
                params.add("%\"$filter\":[%")
                builder.append("tags like ? and ")
            }
            val query = builder.toString().trimEnd()
            val formatted = if (query.endsWith("and")) query.removeSuffix("and") else query
            return SimpleSQLiteQuery("$formatted order by isPinned desc, time desc", params.toTypedArray())
        }
    }
}