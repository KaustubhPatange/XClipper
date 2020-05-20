package com.kpstv.xclipper.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.data.provider.ClipProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepositoryImpl(
    private val clipdao: ClipDataDao,
    private val firebaseProvider: FirebaseProvider,
    private val clipProvider: ClipProvider
) : MainRepository {

    private val TAG = javaClass.simpleName
    private val lock = Any()

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

    override fun updateClip(clip: Clip) {
        Coroutines.io {
            clipdao.update(clip)
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

    override fun updateRepository(data: String?) {
        clipProvider.processClip(data)?.let { clip ->
            saveClip(clip)
            firebaseProvider.uploadData(clip)
        }
    }

}