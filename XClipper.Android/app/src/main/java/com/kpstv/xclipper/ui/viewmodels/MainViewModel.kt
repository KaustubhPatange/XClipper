package com.kpstv.xclipper.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.App
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.localized.dao.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.isClipboardAccessibilityServiceRunning
import com.kpstv.xclipper.extensions.utils.Utils.Companion.loginToDatabase
import com.kpstv.xclipper.extensions.utils.Utils.Companion.logoutFromDatabase
import com.kpstv.xclipper.ui.helpers.DictionaryApiHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import com.kpstv.xclipper.ui.viewmodels.managers.MainEditManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainSearchManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainStateManager
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel @ViewModelInject constructor(
    application: Application,
    private val mainRepository: MainRepository,
    private val tagRepository: TagDao,
    private val preferenceProvider: PreferenceProvider,
    private val firebaseProvider: FirebaseProvider,
    private val clipboardProvider: ClipboardProvider,
    private val dbConnectionProvider: DBConnectionProvider,
    private val firebaseUtils: FirebaseUtils,
    val dictionaryApiHelper: DictionaryApiHelper,
    val tinyUrlApiHelper: TinyUrlApiHelper,
    val editManager: MainEditManager,
    val searchManager: MainSearchManager,
    val stateManager: MainStateManager
) : AndroidViewModel(application) {
    val context: Context = application.applicationContext

    private val TAG = javaClass.simpleName

    private val _clipLiveData = MutableLiveData<List<Clip>>()

    private val _tagMapLiveData = MutableLiveData<List<TagMap>>()

    private val _tagLiveData = MutableLiveData<List<Tag>>()

    val currentClip: LiveData<String>
        get() = clipboardProvider.getCurrentClip()

    private val mediatorLiveData = MediatorLiveData<List<Clip>>()

    val clipLiveData: LiveData<List<Clip>>
        get() = mediatorLiveData

    val tagCountData: LiveData<List<TagMap>>
        get() = _tagMapLiveData

    val tagLiveData: LiveData<List<Tag>>
        get() = _tagLiveData

    fun refreshRepository() {
        Coroutines.io {
            _clipLiveData.postValue(mainRepository.getAllData())
        }
    }

    fun postToRepository(clip: Clip) {
        mainRepository.updateRepository(clip)
    }

    fun changeClipPin(clip: Clip?, boolean: Boolean) {
        mainRepository.updatePin(clip, boolean)
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
        if (oldClip.data != newClip.data)
            firebaseProvider.replaceData(oldClip, newClip)
    }

    fun makeAValidationRequest(statusListener: StatusListener) {
        mainRepository.validateData(statusListener)
    }

    fun postToTagRepository(tag: Tag) {
        Coroutines.io { tagRepository.insertTag(tag) }
    }

    fun deleteFromTagRepository(tag: Tag, statusListener: StatusListener) {
        mainRepository.checkForDependent(tag.name,
            RepositoryListener(
                dataExist = {
                    statusListener.onError()
                },
                notFound = {
                    statusListener.onComplete()
                    Coroutines.io { tagRepository.delete(tag) }
                }
            ))
    }

    fun updateDeviceConnection(options: FBOptions, responseListener: ResponseListener<Unit>) {
        dbConnectionProvider.saveOptionsToAll(options)
        firebaseProvider.addDevice(App.DeviceID, ResponseListener(
            complete = {
                loginToDatabase(
                    preferenceProvider = preferenceProvider,
                    dbConnectionProvider = dbConnectionProvider,
                    options = options
                )
                responseListener.onComplete(Unit)
            },
            error = {
                dbConnectionProvider.detachDataFromAll()
                responseListener.onError(it)
            }
        ))
    }

    fun removeDeviceConnection(responseListener: ResponseListener<Unit>) {
        firebaseProvider.removeDevice(App.DeviceID, ResponseListener(
            complete = {
                logoutFromDatabase(
                    context = context,
                    preferenceProvider = preferenceProvider,
                    dbConnectionProvider = dbConnectionProvider
                )
                responseListener.onComplete(Unit)
            },
            error = {
                logoutFromDatabase(
                    context = context,
                    preferenceProvider = preferenceProvider,
                    dbConnectionProvider = dbConnectionProvider
                )
                responseListener.onError(it)
            }
        ))
    }

    init {
        mainRepository.getAllLiveClip().observeForever { clips ->
            _clipLiveData.postValue(clips)

            val list = ArrayList<TagMap>()
            clips.forEach { clip ->
                clip.tags?.forEach { e ->
                    val find = list.find { it.name == e.key }
                    if (find != null) {
                        find.count++
                    } else {
                        list.add(TagMap(e.key, 1))
                    }
                }
            }
            _tagMapLiveData.value = list
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

        /** This is to observe the device validation when accessibility service is off. */
        if (!isClipboardAccessibilityServiceRunning(application.applicationContext)) {
            firebaseUtils.observeDatabaseInitialization()
            clipboardProvider.observeClipboardChange()
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
                    if (!clip.data?.toLowerCase(Locale.getDefault())
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
                        clip.data?.toLowerCase(Locale.getDefault())?.contains(searchText)
                            ?: false
                    }
                )

            } else
                mediatorLiveData.postValue(list.toList())
        }
    }
}