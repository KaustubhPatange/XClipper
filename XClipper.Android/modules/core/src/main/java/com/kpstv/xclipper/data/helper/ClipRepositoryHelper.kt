package com.kpstv.xclipper.data.helper

import com.kpstv.xclipper.data.model.Clip

interface ClipRepositoryHelper {
    fun insertOrUpdateClip(clip: Clip, toNotify: Boolean = true, toFirebase: Boolean = true)
    fun insertOrUpdateClip(data: String, toNotify: Boolean = true, toFirebase: Boolean = true)
    fun insertOrUpdateClip(clips: List<Clip>, toFirebase: Boolean = true, toNotify: Boolean = true, notifyOffset: Int = 5)
    fun deleteClip(clipData: List<String>?)
}