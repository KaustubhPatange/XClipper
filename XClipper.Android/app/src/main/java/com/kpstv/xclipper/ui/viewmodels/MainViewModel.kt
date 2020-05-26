package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.data.repository.TagRepository
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import com.kpstv.xclipper.ui.viewmodels.managers.MainEditManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainSearchManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainStateManager
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(
    application: Application,
    private val mainRepository: MainRepository,
    private val tagRepository: TagRepository,
    private val firebaseProvider: FirebaseProvider,
    val dictionaryApiHelper: DictionaryApiHelper,
    val tinyUrlApiHelper: TinyUrlApiHelper
) : AndroidViewModel(application) {

    private val TAG = javaClass.simpleName
    private var _tag: Tag? = null
    private val _stateManager =
        MainStateManager()
    private val _searchManager =
        MainSearchManager()
    private val _editManager =
        MainEditManager(tagRepository)
    private val _clipLiveData = MutableLiveData<List<Clip>>()

    private val _tagLiveData = MutableLiveData<List<Tag>>()

    fun setTag(tag: Tag) {
        _tag = tag
    }

    fun getTag(): Tag? {
        return _tag
    }

    val stateManager: MainStateManager
        get() = _stateManager

    val searchManager: MainSearchManager
        get() = _searchManager

    val editManager: MainEditManager
        get() = _editManager

    private val mediatorLiveData = MediatorLiveData<List<Clip>>()

    val clipLiveData: LiveData<List<Clip>>
        get() = mediatorLiveData

    val tagLiveData: LiveData<List<Tag>>
        get() = _tagLiveData

    fun postToRepository(unencryptedData: String) {
        mainRepository.updateRepository(unencryptedData)
    }

    fun postToRepository(clip: Clip) {
        mainRepository.processClipAndSave(clip)
    }

    fun checkForDuplicateClip(unencryptedData: String, repositoryListener: RepositoryListener) {
        mainRepository.checkForDuplicate(unencryptedData, repositoryListener)
    }

    fun checkForDuplicateClip(
        unencryptedData: String,
        id: Int,
        repositoryListener: RepositoryListener
    ) {
        mainRepository.checkForDuplicate(unencryptedData, id, repositoryListener)
    }

    fun deleteFromRepository(clip: Clip) {
        mainRepository.deleteClip(clip)
    }

    fun deleteMultipleFromRepository(clips: List<Clip>) {
        mainRepository.deleteMultiple(clips)
    }

    fun postUpdateToRepository(oldClip: Clip, newClip: Clip) {
        mainRepository.updateClip(newClip, FilterType.Id)
        if (oldClip.data?.Decrypt() != newClip.data?.Decrypt())
            firebaseProvider.replaceData(oldClip, newClip)
    }

    fun makeAValidationRequest(statusListener: StatusListener) {
        mainRepository.validateData(statusListener)
    }

    fun postToTagRepository(tag: Tag) {
        tagRepository.insertTag(tag)
    }

    fun deleteFromTagRepository(tag: Tag, statusListener: StatusListener) {
        mainRepository.checkForDependent(tag.name,
            RepositoryListener(
                dataExist = {
                    statusListener.onError()
                },
                notFound = {
                    statusListener.onComplete()
                    tagRepository.deleteTag(tag)
                }
            ))
    }

    init {

        mainRepository.getAllLiveClip().observeForever {
            _clipLiveData.postValue(it)
        }

        tagRepository.getAllLiveData().observeForever {
            _tagLiveData.postValue(it)
        }

        mediatorLiveData.addSource(searchManager.searchString) {
            makeMySource(
                _clipLiveData.value,
                searchManager.searchFilters.value,
                searchManager.tagFilters.value,
                it
            )
        }

        mediatorLiveData.addSource(searchManager.tagFilters) {
            makeMySource(
                _clipLiveData.value,
                searchManager.searchFilters.value,
                it,
                searchManager.searchString.value
            )
        }

        mediatorLiveData.addSource(searchManager.searchFilters) {
            makeMySource(
                _clipLiveData.value,
                it,
                searchManager.tagFilters.value,
                searchManager.searchString.value
            )
        }

        mediatorLiveData.addSource(_clipLiveData) {
            makeMySource(
                it,
                searchManager.searchFilters.value,
                searchManager.tagFilters.value,
                searchManager.searchString.value
            )
        }
    }

    private fun makeMySource(
        mainList: List<Clip>?,
        searchFilter: ArrayList<String>?,
        tagFilter: ArrayList<Tag>?,
        searchText: String?
    ) {
        if (mainList != null) {
            val list = ArrayList<Clip>(mainList)

            mainList.forEach { clip ->
                searchFilter?.forEach inner@{ filter ->
                    if (!clip.data?.Decrypt()?.toLowerCase(Locale.getDefault())
                            ?.contains(filter)!!
                    ) {
                        list.remove(clip)
                        return@inner
                    }
                }
                tagFilter?.forEach inner@{ tag ->
                    if (!clip.tags?.keys.isNullOrEmpty() && !clip.tags?.keys?.contains(tag.name)!!) {
                        list.remove(clip)
                        return@inner
                    }
                }
            }

            if (!searchText.isNullOrBlank()) {
                mediatorLiveData.postValue(
                    list.filter { clip ->
                        clip.data?.Decrypt()?.toLowerCase(Locale.getDefault())?.contains(searchText)
                            ?: false
                    }
                )

            } else
                mediatorLiveData.postValue(list.toList())
        }
    }

}