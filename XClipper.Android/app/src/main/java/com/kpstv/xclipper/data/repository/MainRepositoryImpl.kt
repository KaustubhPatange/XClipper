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

class MainRepositoryImpl(
    private val clipdao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider,
    private val clipProvider: ClipProvider
) : MainRepository {

    private val TAG = javaClass.simpleName
    private val lock = Any()
    private val lock1 = Any()

    init {
        /*clipdao.getAllLiveData().observeForever {

        }*/
    }

    override fun saveClip(clip: Clip?) {
        if (clip == null) return;
        Coroutines.io {
            /**
             *  Synchronization is needed since, sometimes accessibility services automatically
             *  try to save data twice.
             */
            synchronized(lock) {
                val allClips = clipdao.getAllData()

                if (allClips.isNotEmpty()) {
                    allClips.filter { it.data?.Decrypt() == clip.data?.Decrypt() }.forEach {
                        clipdao.delete(it)
                    }
                }
                clipdao.insert(clip)
                Log.e(TAG, "Data Saved: ${clip.data?.Decrypt()}")
            }
        }
    }

    /**
     * TODO: There is a wrong insertion of data
     *
     * Since save clip function does work on separate thread, synchronization
     * between these threads on quick insertion is creating uneven insertions.
     */
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

    /**
     * This method will either update the clip or insert the clip.
     *
     * When the data is coming from firebase saving it directly i.e overwriting it
     * might loose our existing custom tags on it. So this approach only updates
     * data & time with the existing clip in database.
     *
     * If it does not exist then we will process and save it.
     *
     * @param Clip A clip which is contains encrypted data and no tags (usually coming from firebase).
     */
    override fun updateClip(clip: Clip?) {
        if (clip == null) return

        Coroutines.io {
            /**
             *  Synchronization is needed since, sometimes accessibility services automatically
             *  try to update data twice.
             */
            synchronized(lock1) {
                val allData = clipdao.getAllData()

                if (allData.isNotEmpty()) {
                    val innerClip = allData.firstOrNull { it.data?.Decrypt() == clip.data?.Decrypt() }
                    if (innerClip != null) {
                        Log.e(TAG, "Update Clip Id: ${innerClip.id}")
                        clipdao.update(innerClip.id!!, clip.data!!, clip.time!!)
                        return@io
                    }
                }
                processClipAndSave(clip)
            }
        }
    }

    override fun deleteClip(clip: Clip) {
        Coroutines.io {
            clipdao.delete(clip.id!!)
            firebaseProvider.deleteData(clip)
        }
    }

    override fun deleteMultiple(clips: List<Clip>) {
        Coroutines.io {
            for (clip in clips)
                clipdao.delete(clip)
            firebaseProvider.deleteMultipleData(clips)
        }
    }

    override fun getAllLiveClip(): LiveData<List<Clip>> {
        return clipdao.getAllLiveData()
    }

    override fun getAllData(): List<Clip> {
        return clipdao.getAllData()
    }

    override fun processClipAndSave(clip: Clip?) {
        clipProvider.processClip(clip?.data?.Decrypt())?.let { item ->
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