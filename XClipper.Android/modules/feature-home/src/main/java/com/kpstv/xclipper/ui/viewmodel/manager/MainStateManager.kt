package com.kpstv.xclipper.ui.viewmodel.manager

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extension.enumeration.DialogState
import com.kpstv.xclipper.extension.enumeration.ToolbarState
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("NullSafeMutableLiveData")
@Singleton
class MainStateManager @Inject constructor() {

    private val TAG = javaClass.simpleName

    private val _toolbarState: MutableLiveData<ToolbarState> =
        MutableLiveData(ToolbarState.NormalViewState)

    private val _dialogState: MutableLiveData<DialogState> =
        MutableLiveData(DialogState.Normal)

    private val _selectedItemClips = MutableLiveData<List<Clip>>()
    private val _isMultiSelectionEnabled = MutableLiveData<Boolean>()

    private val _selectedItem = MutableLiveData<Clip>()

    val selectedItemClips: LiveData<List<Clip>>
        get() = _selectedItemClips

    val toolbarState: LiveData<ToolbarState>
        get() = _toolbarState

    val dialogState: LiveData<DialogState>
        get() = _dialogState

    val selectedItem: LiveData<Clip>
        get() = _selectedItem

    fun setToolbarState(state: ToolbarState) =
        _toolbarState.postValue(state)

    fun setDialogState(state: DialogState) =
        _dialogState.postValue(state)

    fun isEditDialogStateActive() =
        _dialogState.value == DialogState.Edit

    fun isMultiSelectionStateActive(): Boolean =
        _toolbarState.value == ToolbarState.MultiSelectionState

    val multiSelectionState: LiveData<Boolean>
        get() = _isMultiSelectionEnabled

    fun addOrRemoveClipFromSelectedList(clip: Clip) {
        var list = _selectedItemClips.value?.toMutableList()
        if (list == null)
            list = ArrayList()
        else {
            if (list.contains(clip)) {
                list.remove(clip)
            } else {
                list.add(clip)
            }
        }
        _selectedItemClips.postValue(list)
    }

    fun addOrRemoveSelectedItem(clip: Clip) {
        _selectedItem.value?.let {
            if (it == clip) {
                clearSelectedItem()
                return
            }
        }
        _selectedItem.postValue(clip)
    }

    fun clearSelectedItem() {
        _selectedItem.postValue(null)
    }

    fun addAllToSelectedList(clips: ArrayList<Clip>) {
        _selectedItemClips.postValue(clips)
    }

    fun clearSelectedList() {
        _selectedItemClips.postValue(ArrayList())
    }

    init {
        toolbarState.observeForever {
            if (it == ToolbarState.MultiSelectionState) {
                _isMultiSelectionEnabled.postValue(true)
            } else {
                _isMultiSelectionEnabled.postValue(false)
            }
        }
    }
}