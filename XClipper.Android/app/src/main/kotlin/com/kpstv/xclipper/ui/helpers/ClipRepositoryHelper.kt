package com.kpstv.xclipper.ui.helpers

import android.content.Context
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.mainThread
import com.kpstv.xclipper.extensions.toInt
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipRepositoryHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MainRepository,
    private val notificationHelper: NotificationHelper
) {
    fun insertOrUpdateClip(clip: Clip, toNotify: Boolean = true, toFirebase: Boolean = true) {
        insertOrUpdateClip(clip.data, toNotify, toFirebase)
    }

    fun insertOrUpdateClip(data: String, toNotify: Boolean = true, toFirebase: Boolean = true) {
        Coroutines.io {
            val isInserted = repository.updateRepository(data, toFirebase)
            if (isInserted && toNotify)
                sendClipNotification(data)
        }
    }

    /**
     * @param notifyOffset An offset value to show combined (eg: x clips added) instead of individual
     *                     notifications.
     */
    fun insertOrUpdateClip(clips: List<Clip>, toFirebase: Boolean = true, toNotify: Boolean = true, notifyOffset: Int = 5) {
        Coroutines.io {
            val singleNotify = clips.size <= notifyOffset
            var addedClips = 0
            clips.forEach { clip ->
                val isAdded = repository.updateRepository(clip, toFirebase)
                addedClips += isAdded.toInt()
                if (isAdded && singleNotify && toNotify)
                    sendClipNotification(clip)
            }
            if (addedClips > 0 && !singleNotify && toNotify) {
                notificationHelper.pushNotification(
                    text = "$addedClips ${context.getString(R.string.multi_clips_added)}",
                    withActions = false
                )
            }
        }
    }

    fun deleteClip(clipData: List<String>?) {
        Coroutines.io {
            clipData?.forEach { repository.deleteClip(it) }
        }
    }

    private fun sendClipNotification(clip: Clip) {
        sendClipNotification(clip.data)
    }

    private fun sendClipNotification(data: String) {
        mainThread {
            notificationHelper.pushNotification(data)
        }
    }
}