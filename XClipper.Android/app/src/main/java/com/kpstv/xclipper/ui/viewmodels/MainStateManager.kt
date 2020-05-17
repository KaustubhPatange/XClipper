package com.kpstv.xclipper.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Clip

class MainStateManager {

    private val TAG = javaClass.simpleName

    private val _toolbarState: MutableLiveData<ToolbarState> =
        MutableLiveData(ToolbarState.NormalViewState)

    private val _selectedNodes = MutableLiveData<ArrayList<Clip>>()
    private val _isMultiSelectionEnabled = MutableLiveData<Boolean>()

    val selectedNodes: LiveData<ArrayList<Clip>>
        get() = _selectedNodes

    val toolbarState: LiveData<ToolbarState>
        get() = _toolbarState

    fun setToolbarState(state: ToolbarState) =
        _toolbarState.postValue(state)

    fun isMultiSelectionStateActive(): Boolean =
        _toolbarState.value == ToolbarState.MultiSelectionState

    val multiSelectionState: LiveData<Boolean>
        get() = _isMultiSelectionEnabled

    fun addOrRemoveClipFromSelectedList(clip: Clip) {
        var list = _selectedNodes.value
        if (list == null)
            list = ArrayList()
        else {
            if (list.contains(clip)) {
                list.remove(clip)
            } else {
                list.add(clip)
            }
        }
        _selectedNodes.postValue(list)
    }

    fun addAllToSelectedList(clips: ArrayList<Clip>) {
        _selectedNodes.postValue(clips)
    }

    fun clearSelectedList() {
        _selectedNodes.postValue(ArrayList())
    }

    init {
        toolbarState.observeForever {
            if (it == ToolbarState.MultiSelectionState) {
              _isMultiSelectionEnabled.postValue(true)
            }
            else {
               _isMultiSelectionEnabled.postValue(false)
            }

        }
    }
}