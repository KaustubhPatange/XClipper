package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.Status

interface MainRepository {
    fun saveClip(clip: Clip?)

    fun processClipAndSave(clip: Clip?)

    fun updateClip(clip: Clip?)

    fun deleteClip(clip: Clip)

    fun deleteMultiple(clips: List<Clip>)

    fun updateRepository(data: String?)

    fun getAllData(): List<Clip>

    fun validateData(onComplete: (Status) -> Unit)

    fun getAllLiveClip(): LiveData<List<Clip>>
}