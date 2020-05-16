package com.kpstv.xclipper.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.localized.ToolbarState
import com.kpstv.xclipper.data.model.Clip

class MainStateManager {

    private val _toolbarState: MutableLiveData<ToolbarState> =
        MutableLiveData(ToolbarState.NormalViewState)

    private val _selectedNodes = MutableLiveData<ArrayList<Clip>>()

    val selectedNodes: LiveData<ArrayList<Clip>>
        get() = _selectedNodes

    val toolbarState: LiveData<ToolbarState>
        get() = _toolbarState

    fun setToolbarState(state: ToolbarState) =
        _toolbarState.postValue(state)

    fun isMultiSelectionStateActive(): Boolean =
        _toolbarState.value == ToolbarState.MultiSelectionState

    fun addOrRemoveClipFromSelectedList(clip: Clip) {
        var list = _selectedNodes.value
        if (list == null)
            list = ArrayList()
        else {
            if (list.contains(clip)) {
                list.remove(clip)
            }
            else {
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
}