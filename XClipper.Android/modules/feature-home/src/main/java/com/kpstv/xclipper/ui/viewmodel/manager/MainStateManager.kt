package com.kpstv.xclipper.ui.viewmodel.manager

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.extension.enumeration.DialogState
import com.kpstv.xclipper.extension.enumeration.ToolbarState
import com.kpstv.xclipper.ui.adapter.ClipAdapterItem
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

    private val _selectedItemClips = MutableLiveData<List<ClipAdapterItem>>()
    private val _isMultiSelectionEnabled = MutableLiveData<Boolean>()

    private val _expandedItem = MutableLiveData<ClipAdapterItem>()

    val selectedItemClips: LiveData<List<ClipAdapterItem>>
        get() = _selectedItemClips

    val toolbarState: LiveData<ToolbarState>
        get() = _toolbarState

    val dialogState: LiveData<DialogState>
        get() = _dialogState

    val expandedItem: LiveData<ClipAdapterItem>
        get() = _expandedItem

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

    fun addOrRemoveClipFromSelectedList(clip: ClipAdapterItem) {
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

    fun addOrRemoveExpandedItem(clip: ClipAdapterItem) {
        _expandedItem.value?.let {
            if (it == clip) {
                clearExpandedItem()
                return
            }
        }
        _expandedItem.postValue(clip)
    }

    fun clearExpandedItem() {
        _expandedItem.postValue(null)
    }

    fun addAllToSelectedList(clips: List<ClipAdapterItem>) {
        _selectedItemClips.postValue(clips)
    }

    fun clearSelectedList() {
        _selectedItemClips.postValue(emptyList())
    }

    fun repostMultiSelectionState() {
        _isMultiSelectionEnabled.postValue(_isMultiSelectionEnabled.value)
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