package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Status
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class MainViewModel(
    application: Application,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider
) : AndroidViewModel(application) {

    private val TAG = javaClass.simpleName
    private val _stateManager = MainStateManager()
    private val _searchManager = MainSearchManager()
    private val _clipLiveData = MutableLiveData<List<Clip>>()

    val stateManager: MainStateManager
        get() = _stateManager

    val searchManager: MainSearchManager
        get() = _searchManager

    private val mediatorLiveData = MediatorLiveData<List<Clip>>()

    val clipLiveData: LiveData<List<Clip>>
        get() = mediatorLiveData

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

    fun makeAValidationRequest(block: (Status) -> Unit) {
        repository.validateData(block)
    }

    init {

        repository.getAllLiveClip().observeForever {
            _clipLiveData.postValue(it)
        }

        mediatorLiveData.addSource(searchManager.searchString) {
            makeMySource(_clipLiveData.value, searchManager.searchFilters.value, it)
        }

        mediatorLiveData.addSource(searchManager.searchFilters) {
            makeMySource(_clipLiveData.value, it, searchManager.searchString.value)
        }

        mediatorLiveData.addSource(_clipLiveData) {
            makeMySource(it, searchManager.searchFilters.value, searchManager.searchString.value)
        }
    }

    private fun makeMySource(mainList: List<Clip>?, searchFilter: ArrayList<String>?, searchText: String?) {
        if (mainList != null) {
            val list = ArrayList<Clip>(mainList)

            mainList.forEach { clip ->
                searchFilter?.forEach inner@{ filter ->
                    if (!clip.data?.Decrypt()?.toLowerCase(Locale.getDefault())?.contains(filter)!!) {
                        list.remove(clip)
                        return@inner
                    }
                }
            }

            if (!searchText.isNullOrBlank()) {
                mediatorLiveData.postValue(
                    list.filter { clip ->
                        clip.data?.Decrypt()?.toLowerCase(Locale.getDefault())?.contains(searchText) ?: false
                    }
                )

            }else
                mediatorLiveData.postValue(list.toList())
        }
    }

}