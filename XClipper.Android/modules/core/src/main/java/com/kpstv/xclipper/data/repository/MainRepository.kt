package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.DateFilter
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.enumerations.SpecialTagFilter
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    /**
     * This operation will save data to only local database.
     *
     * During processing it will create the data from "clip.data" parameter.
     */
    suspend fun processClipAndSave(clip: Clip?): Boolean

    /**
     * This method will either update the clip or insert the clip.
     *
     * When the data is coming from firebase saving it directly i.e overwriting it
     * might loose our existing custom tags on it. So this approach only updates
     * data & time with the existing clip in database.
     *
     * If it does not exist then we will process and save it.
     *
     * @param clip A clip which is contains encrypted string data and no [Clip.tags] (usually coming from firebase).
     */
    suspend fun updateClip(clip: Clip?, filterType: FilterType = FilterType.Text): Boolean

    /**
     * The function will change [Clip.isPinned] to the incoming value.
     */
    suspend fun updatePin(clip: Clip?, isPinned: Boolean)
    suspend fun updateTime(clip: Clip?)

    suspend fun deleteClip(clip: Clip, deleteType: DeleteType = DeleteType.ALL)
    suspend fun deleteClip(data: String?, deleteType: DeleteType = DeleteType.ALL)

    suspend fun deleteMultiple(clips: List<Clip>, deleteType: DeleteType = DeleteType.ALL)

    /**
     * Use this function when you are saving data to local as well as firebase
     * database.
     *
     * While saving to database it will delete an existing data (if present)
     * and then create new data from the given string and perform insert operation.
     */
    suspend fun updateRepository(data: String?, toFirebase:Boolean = true): Boolean
    suspend fun updateRepository(clip: Clip, toFirebase:Boolean = true): Boolean

    /**
     * Remove tags specified by the tag names from the clips.
     */
    suspend fun removeTag(tagName: String): Boolean

    suspend fun getClipByData(data: String): Clip?

    /**
     * Checks if the clip exist in the database.
     *
     * @return True if clip is found.
     */
    suspend fun checkForDuplicate(data: String?): Boolean
    suspend fun checkForDuplicate(data: String?, id: Int): Boolean

    fun getDataSource(wildcard: String = "%", pagingSize: Int = 10): LiveData<PagedList<Clip>>

    suspend fun syncDataFromRemote(): Boolean

    fun getTotalCount(): LiveData<Int>

    fun getAllLiveClip(): LiveData<List<Clip>>
    fun getAllTags(): Flow<List<TagMap>>

    fun getDataByFilter(
        searchText: String = "",
        searchFilters: List<String> = emptyList(),
        dateFilter: DateFilter? = null,
        tagFilters: List<Tag> = emptyList(),
        specialTagFilters: List<SpecialTagFilter> = emptyList(),
    ): List<Clip>

    /**
     * Check if the data is the last data i.e the new data in the list.
     */
    suspend fun isTopData(data: String) : Boolean

    enum class DeleteType {
        /** Delete only local clips. */
        LOCAL,
        /** Delete only remote clips */
        REMOTE,
        /** Delete both from local as well as from remote. */
        ALL;

        fun canDeleteLocal(): Boolean = this == LOCAL || this == ALL
        fun canDeleteRemote(): Boolean = this == REMOTE || this == ALL
    }
}