package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.ClipProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.Status
import com.kpstv.xclipper.extensions.UpdateType

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
                    allClips.filter { it.data?.Decrypt() == clip.data?.Decrypt() }.forEach {
                        clipDao.delete(it)
                    }
                }
                clipDao.insert(clip)
                Log.e(TAG, "Data Saved: ${clip.data?.Decrypt()}")
            }
        }
    }


    override fun validateData(onComplete: (Status) -> Unit) {
        firebaseProvider.clearData()
        firebaseProvider.getAllClipData {
            if (it == null) {
                onComplete.invoke(Status.Error)
                return@getAllClipData
            }

            it.forEach { clip ->
                updateClip(clip)
            }

            onComplete.invoke(Status.Success)
        }
    }

    override fun updateClip(clip: Clip?, updateType: UpdateType) {
        if (clip == null) return

        Coroutines.io {
            /**
             *  Synchronization is needed since, sometimes accessibility services automatically
             *  try to update data twice.
             */
            synchronized(lock1) {
                val allData = clipDao.getAllData()

                if (allData.isNotEmpty()) {
                    val innerClip: Clip? = if (updateType == UpdateType.Text)
                        allData.firstOrNull { it.data?.Decrypt() == clip.data?.Decrypt() }
                    else
                        allData.firstOrNull { it.id == clip.id }
                    if (innerClip != null) {
                        Log.e(TAG, "Update Clip Id: ${innerClip.id}")

                        clipProvider.processClip(Clip(innerClip.id!!, clip.data!!, clip.time!!))?.let {
                            clipDao.update(it)
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

}