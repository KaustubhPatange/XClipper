package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.LOCAL_MAX_ITEM_STORAGE
import com.kpstv.xclipper.App.MAX_CHARACTER_TO_STORE
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.clone
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepositoryImpl(
    private val clipDao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider,
    private val notificationHelper: NotificationHelper
) : MainRepository {

    private val TAG = javaClass.simpleName
    private val lock = Any()
    private val lock1 = Any()

    var data: LiveData<PagedList<Clip>>? = null

    init {
        clipDao.getAllLiveData().observeForever {
            data
        }
        val map = clipDao.getDataSource().map { clip -> clip }
    }

    override fun getDataSource() =
        clipDao.getDataSource().toLiveData(10)

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

                    /** Let's filter the clip for required items only **/
                    if (allClips.size > LOCAL_MAX_ITEM_STORAGE) {
                        clipDao.delete(allClips.first())
                    }
                }
                clipDao.insert(clip)

                /** Send a notification */
                mainThread { notificationHelper.pushNotification(clip.data?.Decrypt()!!) }

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

                        val finalClip = Clip.from(clip.clone(innerClip.id!!))

                        /** Merge the existing tags into the clip tags */
                        if (finalClip.tags != null && clip.tags != null)
                            finalClip.tags = (finalClip.tags!! + clip.tags!!)

                        clipDao.update(finalClip)

                        /*clipProvider.processClip(
                            clip.clone(innerClip.id!!)
                        )?.let { finalClip ->
                            */
                        /** Merge the existing tags into the clip tags *//*
                            if (finalClip.tags != null && clip.tags != null)
                                finalClip.tags = (finalClip.tags!! + clip.tags!!)
                            clipDao.update(finalClip)
                        }*/
                        return@io
                    }
                }
                processClipAndSave(clip)
            }
        }
    }

    override fun updatePin(clip: Clip?, isPinned: Boolean) {
        if (clip == null) return
        Coroutines.io {
            clipDao.update(clip.apply {
                this.isPinned = isPinned
            })
        }
    }

    override fun deleteClip(clip: Clip) {
        Coroutines.io {
            clipDao.delete(clip.id!!)
            firebaseProvider.deleteData(clip)
        }
    }

    override fun deleteClip(unencryptedData: String?) {
        Coroutines.io {
            val clipToFind =
                clipDao.getAllData().firstOrNull { it.data?.Decrypt() == unencryptedData }
            if (clipToFind != null)
                deleteClip(clipToFind)
        }
    }

    override fun deleteLast() {
        Coroutines.io {
            clipDao.deleteLast()
        }
    }

    override fun deleteMultiple(clips: List<Clip>) {
        Coroutines.io {
            for (clip in clips)
                clipDao.delete(clip)
            firebaseProvider.deleteMultipleData(clips)
        }
    }

    override fun checkForDuplicate(
        unencryptedData: String?,
        repositoryListener: RepositoryListener
    ) {
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

    override fun checkForDuplicate(
        unencryptedData: String?,
        id: Int,
        repositoryListener: RepositoryListener
    ) {
        Coroutines.io {
            if (clipDao.getAllData()
                    .count { it.data?.Decrypt() == unencryptedData && it.id != id } > 0
            ) Coroutines.main {
                repositoryListener.onDataExist()
            }
            else Coroutines.main {
                repositoryListener.onDataError()
            }
        }
    }

    override fun checkForDependent(tagName: String, repositoryListener: RepositoryListener) {
        Coroutines.io {
            if (clipDao.getAllData()
                    .firstOrNull { it.tags?.keys?.contains(tagName) == true } != null
            ) {
                Coroutines.main {
                    repositoryListener.onDataExist()
                }
            } else
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

    override suspend fun getData(unencryptedText: String) =
        withContext(Dispatchers.IO) {
            clipDao.getAllData().firstOrNull { it.data?.Decrypt() == unencryptedText }
        }

    override fun processClipAndSave(clip: Clip?) {
        if (clip == null) return

        val item = Clip.from(clip)
        saveClip(item)
        /*clipProvider.processClip(clip)?.let { item ->
        }*/
    }

    override fun updateRepository(unencryptedData: String?) {
        if (unencryptedData != null && unencryptedData.length > MAX_CHARACTER_TO_STORE) return

        val clip = Clip.from(unencryptedData!!)

        saveClip(clip)
        firebaseProvider.uploadData(clip)
        /*
        clipProvider.processClip(unencryptedData)?.let { clip ->


            *//*     *//*
            */
        /** Send a notification *//**//*
            mainThread { notificationHelper.pushNotification(clip.data?.Decrypt()!!) }*//*
        }*/
    }

    override fun updateRepository(clip: Clip) {
        val finalClip = Clip.from(clip)

        saveClip(finalClip)
        firebaseProvider.uploadData(finalClip)


        /*  clipProvider.processClip(clip)?.let { innerClip ->
              saveClip(innerClip)
              firebaseProvider.uploadData(innerClip)
  *//*
            *//*
            */
        /** Send a notification *//**//*
           mainThread { notificationHelper.pushNotification(clip.data?.Decrypt()!!) }*//*
        }*/
    }

}