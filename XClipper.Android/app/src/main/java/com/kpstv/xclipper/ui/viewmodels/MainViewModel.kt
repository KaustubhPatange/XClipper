package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.lazyDeferred

class MainViewModel(
    application: Application,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider
) : AndroidViewModel(application) {

    private val TAG = javaClass.name
    private val _stateManager = MainStateManager()

    val stateManager: MainStateManager
        get() = _stateManager

    val clipLiveData by lazyDeferred {
        repository.getAllLiveClip()
    }

    fun postToRepository(data: String) {
        repository.updateRepository(data)
    }

    fun deleteFromRepository(clip: Clip) {
        repository.deleteClip(clip)
    }

    fun deleteMultipleFromRepository(clips: List<Clip>) {
        repository.deleteMultiple(clips)
    }

    fun postUpdateToRepository(oldClip: Clip, newClip: Clip) {
        repository.updateClip(newClip)
        firebaseProvider.replaceData(oldClip, newClip)
    }

}