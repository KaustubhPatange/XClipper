package com.kpstv.xclipper.data.helper

import android.content.Context
import android.util.Log
import com.kpstv.core.BuildConfig
import com.kpstv.core.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.di.notifications.ClipboardNotification
import com.kpstv.xclipper.extensions.launchInIO
import com.kpstv.xclipper.extensions.launchInMain
import com.kpstv.xclipper.extensions.toInt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class ClipRepositoryHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardNotification: ClipboardNotification,
    private val repository: MainRepository,
) {
    private val pendingClipData = ArrayDeque<String>()

    fun insertOrUpdateClip(clip: Clip, toNotify: Boolean = true, toFirebase: Boolean = true) {
        insertOrUpdateClip(clip.data, toNotify, toFirebase)
    }

    fun insertOrUpdateClip(data: String, toNotify: Boolean = true, toFirebase: Boolean = true) {
        CoroutineScope(Dispatchers.IO).launch {
            internalInsertOrUpdateClip(data, toNotify, toFirebase)
        }
    }

    // A concurrent way to inserting clips into database.
    private suspend fun internalInsertOrUpdateClip(data: String?, toNotify: Boolean = true, toFirebase: Boolean = true) {
        if (data == null) return
        if (pendingClipData.contains(data)) return
        pendingClipData.addLast(data)
        if (pendingClipData.size > 1) return

        val time = measureTimeMillis {
            val isInserted = repository.updateRepository(data, toFirebase)
            if (isInserted && toNotify)
                sendClipNotification(data)
        }
        if (BuildConfig.DEBUG)
            Log.e(this::class.simpleName, "Data: ${data.take(50)}, Time taken: $time ms")

        pendingClipData.removeFirstOrNull()
        if (pendingClipData.isNotEmpty()) internalInsertOrUpdateClip(pendingClipData.firstOrNull())
    }

    /**
     * @param notifyOffset An offset value to show combined (eg: x clips added) instead of individual
     *                     notifications.
     */
    fun insertOrUpdateClip(clips: List<Clip>, toFirebase: Boolean = true, toNotify: Boolean = true, notifyOffset: Int = 5) {
        launchInIO {
            val singleNotify = clips.size <= notifyOffset
            var addedClips = 0
            clips.forEach { clip ->
                val isAdded = repository.updateRepository(clip, toFirebase)
                addedClips += isAdded.toInt()
                if (isAdded && singleNotify && toNotify)
                    sendClipNotification(clip)
            }
            if (addedClips > 0 && !singleNotify && toNotify) {
                sendClipNotification(
                    data = context.getString(R.string.multi_clips_added, addedClips.toString()),
                    withAction = false
                )
            }
        }
    }

    fun deleteClip(clipData: List<String>?) {
        launchInIO {
            clipData?.forEach { repository.deleteClip(it) }
        }
    }

    private fun sendClipNotification(clip: Clip) {
        sendClipNotification(clip.data)
    }

    private fun sendClipNotification(data: String, withAction: Boolean = true) {
        launchInMain {
            clipboardNotification.notifyOnCopy(data, withAction)
        }
    }
}