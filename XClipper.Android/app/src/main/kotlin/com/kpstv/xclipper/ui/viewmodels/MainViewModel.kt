package com.kpstv.xclipper.ui.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.localized.dao.ClipDataDao
import com.kpstv.xclipper.data.localized.dao.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.data.model.TagMap
import com.kpstv.xclipper.data.provider.ClipboardProvider
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.listeners.ResponseResult
import com.kpstv.xclipper.extensions.listeners.StatusListener
import com.kpstv.xclipper.extensions.utils.DeviceUtils
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.utils.Utils.Companion.loginToDatabase
import com.kpstv.xclipper.extensions.utils.Utils.Companion.logoutFromDatabase
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.ClipRepositoryHelper
import com.kpstv.xclipper.ui.helpers.TinyUrlApiHelper
import com.kpstv.xclipper.ui.viewmodels.managers.MainEditManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainSearchManager
import com.kpstv.xclipper.ui.viewmodels.managers.MainStateManager
import com.zhuinden.livedatacombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    firebaseUtils: FirebaseUtils,
    clipRepositoryHelper: ClipRepositoryHelper,
    private val mainRepository: MainRepository,
    private val tagRepository: TagDao,
    private val preferenceProvider: PreferenceProvider,
    private val firebaseProvider: FirebaseProvider,
    private val clipboardProvider: ClipboardProvider,
    private val dbConnectionProvider: DBConnectionProvider,
    private val appSettings: AppSettings,
    val tinyUrlApiHelper: TinyUrlApiHelper,
    val editManager: MainEditManager,
    val searchManager: MainSearchManager,
    val stateManager: MainStateManager
) : ViewModel() {
    private val TAG = javaClass.simpleName

    val viewModelIOContext = viewModelScope.coroutineContext + Dispatchers.IO

    val currentClip: LiveData<String>
        get() = clipboardProvider.getCurrentClip()

    private val mutableClipLiveData = MutableStateFlow<List<Clip>>(emptyList())

    val clipLiveData: LiveData<List<Clip>> = mutableClipLiveData.asLiveData(viewModelIOContext)

    val tagCountData: LiveData<List<TagMap>> = mainRepository.getAllTags().asLiveData(viewModelIOContext)

    val tagLiveData: LiveData<List<Tag>> = tagRepository.getAllLiveData().asLiveData(viewModelIOContext)

    fun postToRepository(clip: Clip) {
        CoroutineScope(viewModelIOContext).launch { mainRepository.updateRepository(clip, toFirebase = true) }
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
        CoroutineScope(viewModelIOContext).launch { mainRepository.deleteClip(clip) }
    }

    fun deleteMultipleFromRepository(clips: List<Clip>) {
        CoroutineScope(viewModelIOContext).launch { mainRepository.deleteMultiple(clips) }
    }

    fun postUpdateToRepository(oldClip: Clip, newClip: Clip) {
        CoroutineScope(viewModelIOContext).launch {
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
        viewModelScope.launch {
            tagRepository.delete(tag)
        }
    }

    fun updateDeviceConnection(context: Context, options: FBOptions, responseListener: ResponseListener<Unit>) {
        viewModelScope.launch {
            dbConnectionProvider.saveOptionsToAll(options)
            val result = firebaseProvider.addDevice(DeviceUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    loginToDatabase(
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider,
                        options = options
                    )
                    responseListener.onComplete(Unit)
                }
                is ResponseResult.Error -> {
                    dbConnectionProvider.detachDataFromAll()
                    responseListener.onError(result.error)
                }
            }
        }
    }

    fun removeDeviceConnection(context: Context, responseListener: ResponseListener<Unit>) {
        viewModelScope.launch {
            val result = firebaseProvider.removeDevice(DeviceUtils.getDeviceId(context))
            when (result) {
                is ResponseResult.Complete -> {
                    logoutFromDatabase(
                        context = context,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onComplete(Unit)
                }
                is ResponseResult.Error -> {
                    logoutFromDatabase(
                        context = context,
                        appSettings = appSettings,
                        dbConnectionProvider = dbConnectionProvider
                    )
                    responseListener.onError(result.error)
                }
            }
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
                mutableClipLiveData.emit(list)
            }
        }

        /** Methods optimized to invoke only once regardless of calling from multiple sites. */
        firebaseUtils.observeDatabaseInitialization()
        clipboardProvider.observeClipboardChange(
            action = { data ->
                clipRepositoryHelper.insertOrUpdateClip(data)
                return@observeClipboardChange true
            }
        )
    }
}