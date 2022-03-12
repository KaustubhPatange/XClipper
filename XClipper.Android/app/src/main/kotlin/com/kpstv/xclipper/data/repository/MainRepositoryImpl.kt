package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.sqlite.db.SupportSQLiteQuery
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.PartialClipTagMap
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.extensions.enumerations.FilterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val clipDao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider
) : MainRepository {

    private companion object {
        private const val LOCAL_MAX_ITEM_STORAGE = 200
        private const val MAX_CHARACTER_TO_STORE = 10_000
    }

    private val TAG = javaClass.simpleName

    override fun getDataSource(wildcard: String, pagingSize: Int): LiveData<PagedList<Clip>> = clipDao.getDataSource("%$wildcard%").toLiveData(pagingSize)

    override fun getTotalCount(): LiveData<Int> = clipDao.getTotalCount()

    override fun executeQuery(query: SupportSQLiteQuery): List<Clip> = clipDao.getData(query)

    private suspend fun saveClip(clip: Clip?): Boolean {
        if (clip == null) return false
        if (clipDao.isExist(clip.data)) return false

        if (clipDao.getClipSize() > LOCAL_MAX_ITEM_STORAGE) {
            clipDao.deleteFirst()
        }

        clipDao.insert(clip)

        Log.e(TAG, "Data Saved: ${clip.data}")

        return true
    }

    override suspend fun syncDataFromRemote(): Boolean {
        firebaseProvider.clearData()
        val clips = firebaseProvider.getAllClipData()
        return if (clips == null)
            false
        else {
            clips.forEach { clip ->
                firebaseUpdate(clip)
            }
            true
        }
    }

    /**
     * A private function for updating or adding new data while validation
     */
    private suspend fun firebaseUpdate(clip: Clip): Boolean {
        return if (!clipDao.isExist(clip.data)) {
            /** Insert the data */
            clipDao.insert(Clip.from(clip))
            true
        } else false
    }

    override suspend fun updateClip(clip: Clip?, filterType: FilterType): Boolean {
        if (clip == null) return false

        val innerClip = if (filterType == FilterType.Text)
            clipDao.getData(clip.data)
        else
            clipDao.getData(clip.id)

        return if (innerClip != null) {
            val finalClip = Clip.from(clip.clone(innerClip.id))

            /** Merge the existing tags into the clip tags */
            val newClip = if (finalClip.tags != null && clip.tags != null)
                finalClip.copyWithFields(tags = (finalClip.tags!! + clip.tags!!))
            else
                finalClip

            clipDao.update(newClip)
            true
        } else {
            processClipAndSave(clip)
        }
    }

    override suspend fun updatePin(clip: Clip?, isPinned: Boolean) {
        if (clip == null) return
        clipDao.updatePin(clip.id, isPinned)
    }

    override suspend fun updateTime(clip: Clip?) {
        if (clip == null) return
        clipDao.update(clip.updateTime())
    }

    override suspend fun deleteClip(clip: Clip) {
        clipDao.delete(clip.id)
        firebaseProvider.deleteData(clip)
    }

    override suspend fun deleteClip(data: String?) {
        if (data == null) return
        clipDao.delete(data)
        firebaseProvider.deleteData(Clip.from(data))
    }

    override suspend fun deleteMultiple(clips: List<Clip>) {
        clipDao.delete(clips)
        firebaseProvider.deleteMultipleData(clips)
    }

    override suspend fun checkForDuplicate(data: String?): Boolean {
        if (data == null) return false
        return clipDao.isExist(data)
    }

    override suspend fun checkForDuplicate(data: String?, id: Int): Boolean {
        if (data == null) return false
        val clip = clipDao.getData(data) ?: return false
        return clip.id != id
    }

    override fun getAllLiveClip(): LiveData<List<Clip>> {
        return clipDao.getAllLiveData()
    }

    override suspend fun getClipByData(data: String): Clip? = clipDao.getData(data)

    override suspend fun processClipAndSave(clip: Clip?): Boolean {
        if (clip == null) return false

        val item = Clip.from(clip)
        return saveClip(item)
    }

    override suspend fun updateRepository(data: String?, toFirebase: Boolean): Boolean {
        if (data == null || data.length > MAX_CHARACTER_TO_STORE) return false
        val clip = Clip.from(data)

        val result = saveClip(clip)
        if (toFirebase)
            firebaseProvider.uploadData(clip)

        return result
    }

    override suspend fun updateRepository(clip: Clip, toFirebase: Boolean): Boolean {
        val finalClip = Clip.from(clip)

        val result = saveClip(finalClip)
        if (toFirebase)
            firebaseProvider.uploadData(finalClip)

        return result
    }

    override suspend fun removeTag(tagName: String): Boolean {
        val clips = clipDao.getDataByTag(tagName).filter { it.tags != null }
        val modified = clips.map { clip -> clip.copyWithFields(tags = clip.tags!!.filterNot { it.key == tagName }) }
        clipDao.update(modified)
        return clips.isNotEmpty()
    }

    override fun getAllTags(): Flow<List<TagMap>> {
        return clipDao.getAllTags().transform { value ->
            val tags = value.flatMap { partialTagMap: PartialClipTagMap -> partialTagMap.items.distinctBy { it.key } }
                .groupBy { it.key }
                .map { TagMap(it.key, it.value.size) }
            emit(tags)
        }
    }

    override suspend fun isTopData(data: String): Boolean {
        val topData = clipDao.getTopData()
        return topData != null && topData == data
    }
}