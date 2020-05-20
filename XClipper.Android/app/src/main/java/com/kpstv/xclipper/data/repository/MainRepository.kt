package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Clip

interface MainRepository {
    fun saveClip(clip: Clip?)

    fun updateClip(clip: Clip)

    fun deleteClip(clip: Clip)

    fun deleteMultiple(clips: List<Clip>)

    fun updateRepository(data: String?)

    fun getAllData(): List<Clip>

    fun getAllLiveClip(): LiveData<List<Clip>>
}