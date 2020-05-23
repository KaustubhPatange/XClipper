package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.StatusListener

class MainRepositoryImpl(
    private val clipDao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider,
    private val clipProvider: ClipProvider
) : MainRepository {

    private val TAG = javaClass.simpleName
    private val lock = Any()
    private val lock1 = Any()

    override fun saveClip(clip: Clip?) {
        if (clip == null) return;
        Coroutines.io {
            /**
             *  Synchronization is needed since, sometimes accessibility services automatically
             *  try to save data twice.
             */
            synchronized(lock) {
                val allClips = clipDao.getAllData()

                if (allClips.isNotEmpty()) {
                    val innerClip =
                        allClips.firstOrNull { it.data?.Decrypt() == clip.data?.Decrypt() }
                    if (innerClip != null) return@io
                }
                clipDao.insert(clip)
                Log.e(TAG, "Data Saved: ${clip.data?.Decrypt()}")
            }
        }
    }

    override fun validateData(statusListener: StatusListener) {
        firebaseProvider.clearData()
        firebaseProvider.getAllClipData {
            if (it == null) {
                statusListener.onError()
                return@getAllClipData
            }

            it.forEach { clip ->
                firebaseUpdate(clip)
            }

           statusListener.onComplete()
        }
    }

    /**
     * A private function for updating or adding new data while validation
     */
    private fun firebaseUpdate(clip: Clip) {
        Coroutines.io {
            synchronized(lock1) {
                val allData = clipDao.getAllData()

                val innerClip = allData.firstOrNull { it.data?.Decrypt() == clip.data?.Decrypt() }
                if (innerClip != null) {
                    innerClip.clone(clip.data)
                    clipDao.update(clip)
                } else processClipAndSave(clip)
            }
        }
    }

    override fun updateClip(clip: Clip?, filterType: FilterType) {
        if (clip == null) return

        Coroutines.io {
            /**
             *  Synchronization is needed since, sometimes accessibility services automatically
             *  try to update data twice.
             */
            synchronized(lock1) {
                val allData = clipDao.getAllData()

                if (allData.isNotEmpty()) {
                    val innerClip: Clip? = if (filterType == FilterType.Text)
                        allData.firstOrNull { it.data?.Decrypt() == clip.data?.Decrypt() }
                    else
                        allData.firstOrNull { it.id == clip.id }
                    if (innerClip != null) {
                        Log.e(TAG, "Update Clip Id: ${innerClip.id}")
                        clipProvider.processClip(
                            Clip(
                                id = innerClip.id!!,
                                data = clip.data!!,
                                time = clip.time!!
                            )
                        )?.let { finalClip ->
                            /** Merge the existing tags into the clip tags */
                            if (finalClip.tags != null && clip.tags != null)
                                finalClip.tags = (finalClip.tags!! + clip.tags!!)
                            clipDao.update(finalClip)
                        }
                        return@io
                    }
                }
                processClipAndSave(clip)
            }
        }
    }

    override fun deleteClip(clip: Clip) {
        Coroutines.io {
            clipDao.delete(clip.id!!)
            firebaseProvider.deleteData(clip)
        }
    }

    override fun deleteMultiple(clips: List<Clip>) {
        Coroutines.io {
            for (clip in clips)
                clipDao.delete(clip)
            firebaseProvider.deleteMultipleData(clips)
        }
    }

    override fun checkForDuplicate(unencryptedData: String?, repositoryListener: RepositoryListener) {
        Coroutines.io {
            if (clipDao.getAllData().count { it.data?.Decrypt() == unencryptedData } > 0)
                Coroutines.main {
                    repositoryListener.onDataExist()
                }
            else
                Coroutines.main {
                    repositoryListener.onDataError()
                }
        }
    }

    override fun checkForDependent(tagName: String, repositoryListener: RepositoryListener) {
        Coroutines.io {
            if (clipDao.getAllData().firstOrNull { it.tags?.keys?.contains(tagName) == true } != null) {
                Coroutines.main {
                    repositoryListener.onDataExist()
                }
            }else
                Coroutines.main {
                    repositoryListener.onDataError()
                }
        }
    }

    override fun getAllLiveClip(): LiveData<List<Clip>> {
        return clipDao.getAllLiveData()
    }

    override fun getAllData(): List<Clip> {
        return clipDao.getAllData()
    }

    override fun processClipAndSave(clip: Clip?) {
        clipProvider.processClip(clip)?.let { item ->
            saveClip(item)
        }
    }

    override fun updateRepository(data: String?) {
        clipProvider.processClip(data)?.let { clip ->
            saveClip(clip)
            firebaseProvider.uploadData(clip)
        }
    }

    override fun updateRepository(clip: Clip) {
        clipProvider.processClip(clip)?.let { innerClip ->
            saveClip(innerClip)
            firebaseProvider.uploadData(innerClip)
        }
    }

}