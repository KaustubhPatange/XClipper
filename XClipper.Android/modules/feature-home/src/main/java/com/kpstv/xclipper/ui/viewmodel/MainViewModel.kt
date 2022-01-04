package com.kpstv.xclipper.ui.viewmodel

import androidx.lifecycle.*
import com.kpstv.xclipper.data.helper.ClipRepositoryHelper
import com.kpstv.xclipper.data.helper.FirebaseProviderHelper
import com.kpstv.xclipper.data.localized.ClipDataDao
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extension.listener.RepositoryListener
import com.kpstv.xclipper.extension.listener.StatusListener
import com.kpstv.xclipper.ui.viewmodel.manager.MainEditManager
import com.kpstv.xclipper.ui.viewmodel.manager.MainSearchManager
import com.kpstv.xclipper.ui.viewmodel.manager.MainStateManager
import com.zhuinden.livedatacombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    firebaseProviderHelper: FirebaseProviderHelper,
    clipRepositoryHelper: ClipRepositoryHelper,
    private val mainRepository: MainRepository,
    private val tagRepository: TagDao,
    private val firebaseProvider: FirebaseProvider,
    private val clipboardProvider: ClipboardProvider,
    val editManager: MainEditManager,
    val searchManager: MainSearchManager,
    val stateManager: MainStateManager
) : ViewModel() {
    private val TAG = javaClass.simpleName

    val viewModelIOContext = viewModelScope.coroutineContext + Dispatchers.IO

    val currentClip: LiveData<String>
        get() = clipboardProvider.getCurrentClip()

    private val mutableClipStateFlow = MutableStateFlow<List<Clip>>(emptyList())

    val clipLiveData: LiveData<List<Clip>> = mutableClipStateFlow.asLiveData(viewModelIOContext)

    val tagMapData: LiveData<List<TagMap>> = mutableClipStateFlow.transform { clips ->
        val tags = clips.flatMap { it.tags ?: emptyList() }
            .groupBy { it.key }
            .map { TagMap(it.key, it.value.size) }
        emit(tags)
    }.asLiveData(viewModelIOContext)

    val tagLiveData: LiveData<List<Tag>> = tagRepository.getAllLiveData().asLiveData(viewModelIOContext)

    fun postToRepository(clip: Clip) {
        viewModelScope.launch(viewModelIOContext) { mainRepository.updateRepository(clip, toFirebase = true) }
    }

    fun changeClipPin(clip: Clip?, boolean: Boolean) {
        viewModelScope.launch { mainRepository.updatePin(clip, boolean) }
    }

    fun checkForDuplicateClip(data: String, repositoryListener: RepositoryListener) {
        viewModelScope.launch {
            if (mainRepository.checkForDuplicate(data)) {
                repositoryListener.onDataExist()
            } else {
                repositoryListener.onDataError()
            }
        }
    }

    fun checkForDuplicateClip(data: String, id: Int, repositoryListener: RepositoryListener) {
        viewModelScope.launch {
            if (mainRepository.checkForDuplicate(data, id)) {
                repositoryListener.onDataExist()
            } else {
                repositoryListener.onDataError()
            }
        }
    }

    fun deleteFromRepository(clip: Clip) {
        viewModelScope.launch(viewModelIOContext) { mainRepository.deleteClip(clip) }
    }

    fun deleteMultipleFromRepository(clips: List<Clip>) {
        if (clips.isEmpty()) return
        if (clips.size == 1) {
            deleteFromRepository(clips[0])
            return
        }
        viewModelScope.launch(viewModelIOContext) { mainRepository.deleteMultiple(clips) }
    }

    fun postUpdateToRepository(oldClip: Clip, newClip: Clip) {
        viewModelScope.launch(viewModelIOContext) {
            mainRepository.updateClip(newClip, FilterType.Id)
            if (oldClip.data != newClip.data) {
                firebaseProvider.replaceData(oldClip, newClip)
            }
        }
    }

    fun makeASynchronizeRequest(statusListener: StatusListener) {
        viewModelScope.launch {
            if (mainRepository.syncDataFromRemote()) {
                statusListener.onComplete()
            } else {
                statusListener.onError()
            }
        }
    }

    fun postToTagRepository(tag: Tag) {
        viewModelScope.launch { tagRepository.insertTag(tag) }
    }

    fun deleteFromTagRepository(tag: Tag) {
        viewModelScope.launch(viewModelIOContext) {
            mainRepository.removeTag(tag.name)
            tagRepository.delete(tag)
        }
    }

    init {
        combineTuple(
            mainRepository.getTotalCount(),
            searchManager.searchString,
            searchManager.tagFilters,
            searchManager.searchFilters
        ).observeForever { (_: Int?, searchString: String?, tagFilters: List<Tag>?, searchFilters: List<String>?) ->
            val filter = ClipDataDao.createQuery(searchFilters, tagFilters, searchString)
            CoroutineScope(viewModelIOContext).launch {
                val list = mainRepository.executeQuery(filter)
                mutableClipStateFlow.emit(list)
            }
        }

        /** Methods optimized to invoke only once regardless of calling from multiple sites. */
        firebaseProviderHelper.observeDatabaseInitialization()
        clipboardProvider.observeClipboardChange(
            action = { data ->
                clipRepositoryHelper.insertOrUpdateClip(data)
                return@observeClipboardChange true
            }
        )
    }
}