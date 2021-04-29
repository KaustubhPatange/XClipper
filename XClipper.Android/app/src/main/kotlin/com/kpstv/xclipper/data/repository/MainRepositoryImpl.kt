package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.kpstv.xclipper.App.LOCAL_MAX_ITEM_STORAGE
import com.kpstv.xclipper.App.MAX_CHARACTER_TO_STORE
import com.kpstv.xclipper.data.localized.dao.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val clipDao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider
) : MainRepository {

    private val TAG = javaClass.simpleName

    override fun getDataSource(wildcard: String): LiveData<PagedList<Clip>> = clipDao.getDataSource("%$wildcard%").toLiveData(10)

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
            if (finalClip.tags != null && clip.tags != null)
                finalClip.tags = (finalClip.tags!! + clip.tags!!)

            clipDao.update(finalClip)
            true
        } else {
            processClipAndSave(clip)
        }
    }

    override suspend fun updatePin(clip: Clip?, isPinned: Boolean) {
        if (clip == null) return
        clipDao.updatePin(clip.id, isPinned)
    }

    override suspend fun deleteClip(clip: Clip) {
        clipDao.delete(clip.id)
        firebaseProvider.deleteData(clip)
    }

    override suspend fun deleteClip(data: String?) {
        if (data == null) return
        clipDao.delete(data)
    }

    override suspend fun deleteMultiple(clips: List<Clip>) {
        for (clip in clips)
            clipDao.delete(clip)
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

    override suspend fun checkForDependent(tagName: String): Boolean {
        return clipDao.getAllData()?.firstOrNull { it.tags?.contains(tagName) == true } != null
    }

    override fun getAllLiveClip(): LiveData<List<Clip>> {
        return clipDao.getAllLiveData()
    }

    override suspend fun getAllData(): List<Clip>? {
        return clipDao.getAllData()
    }

    override suspend fun getData(data: String): Clip? = clipDao.getAllData()?.firstOrNull { it.data == data }

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
}