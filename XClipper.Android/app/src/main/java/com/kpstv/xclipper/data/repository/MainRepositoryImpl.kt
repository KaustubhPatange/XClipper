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
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val clipDao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider,
    private val notificationHelper: NotificationHelper
) : MainRepository {

    private val TAG = javaClass.simpleName

    private var notifyEnable = true

    var data: LiveData<PagedList<Clip>>? = null

    init {
        clipDao.getAllLiveData().observeForever {
            data
        }
    }

    override fun getDataSource(): LiveData<PagedList<Clip>> = clipDao.getDataSource().toLiveData(10)

    private suspend fun saveClip(clip: Clip?): Boolean {
        if (clip == null) return false
        if (clipDao.isExist(clip.data)) return false

        if (clipDao.getClipSize() > LOCAL_MAX_ITEM_STORAGE) {
            clipDao.deleteFirst()
        }

        clipDao.insert(clip)

        /** Send a notification */ // TODO: Remove this after words
        if (notifyEnable)
            mainThread { notificationHelper.pushNotification(clip.data) }

        Log.e(TAG, "Data Saved: ${clip.data}")

        return true
    }

    override suspend fun validateData(): Boolean {
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
//        Coroutines.io { // TODO: Check by updating tags.
//            if (clipDao.getAllData()
//                    .count { it.data == unencryptedData && it.id != id } > 0
//            ) Coroutines.main {
//                repositoryListener.onDataExist()
//            }
//            else Coroutines.main {
//                repositoryListener.onDataError()
//            }
//        }
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

        return updateRepository(clip, toFirebase)
    }

    override suspend fun updateRepository(clip: Clip, toFirebase: Boolean): Boolean {
        val finalClip = Clip.from(clip)

        val result = saveClip(finalClip)
        if (toFirebase)
            firebaseProvider.uploadData(finalClip)

        return result
    }

    override fun enableNotify() {
        notifyEnable = true
    }

    override fun disableNotify() {
        notifyEnable = false
    }
}