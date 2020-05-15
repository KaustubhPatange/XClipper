package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Clip

interface MainRepository {
    fun saveClip(clip: Clip?)

    fun updateClip(clip: Clip)

    fun deleteClip(clip: Clip)

    fun updateRepository(data: String?)

    suspend fun getAllLiveClip(): LiveData<List<Clip>>
}