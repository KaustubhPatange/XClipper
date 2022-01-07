package com.kpstv.xclipper.data.localized

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.PartialClipTagMap
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extension.enumeration.SpecialTagFilter
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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(clip: List<Clip>)

    @Delete
    suspend fun delete(clip: Clip)

    @Delete
    suspend fun delete(clip: List<Clip>)

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

    suspend fun getDataByTag(tagName: String): List<Clip> {
        return getData(createTagSearchQuery(tagName))
    }

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
        fun createQuery(searchFilters: List<String>?, tagFilters: List<Tag>?, specialTagFilters: List<SpecialTagFilter>?, searchText: String?): SimpleSQLiteQuery {
            val sqlConditionalLike = if (specialTagFilters?.any { it.isInvert() } == true) "not like" else "like"
            val builder = StringBuilder("select * from table_clip")
            val params = mutableListOf<Any>()
            if (searchText?.isNotEmpty() == true || (searchFilters?.size ?: 0) > 0 || (tagFilters?.size ?: 0) > 0) builder.append(" where ")
            if (searchText != null && searchText.isNotEmpty()) {
                params.add("%$searchText%")
                builder.append("data $sqlConditionalLike ? and ")
            }
            searchFilters?.forEach { filter ->
                params.add("%$filter%")
                builder.append("data $sqlConditionalLike ? and ")
            }
            tagFilters?.map { it.name }?.forEach { filter ->
                params.add("%\"$filter\":[%")
                builder.append("tags $sqlConditionalLike ? and ")
            }
            val query = builder.toString().trimEnd()
            val formatted = if (query.endsWith("and")) query.removeSuffix("and") else query
            return SimpleSQLiteQuery("$formatted order by isPinned desc, time desc", params.toTypedArray())
        }

        // Used to get List<Clip> from tagName present in ClipTagMap
        private fun createTagSearchQuery(tagName: String) : SimpleSQLiteQuery {
            return SimpleSQLiteQuery("select * from table_clip where tags like ?", arrayOf("%\"$tagName\":[%"))
        }
    }
}