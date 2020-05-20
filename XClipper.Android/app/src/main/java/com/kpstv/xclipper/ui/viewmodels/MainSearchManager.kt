package com.kpstv.xclipper.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainSearchManager {

    private var _searchString = MutableLiveData("")
    private var _searchArray = ArrayList<String>()
    private var _searchArrayFilter = MutableLiveData(_searchArray)

    val searchString: LiveData<String>
        get() = _searchString

    val searchFilters: LiveData<ArrayList<String>>
        get() = _searchArrayFilter

    fun setSearchText(text: String) {
        _searchString.postValue(text)
    }

    fun addOrRemoveSearchFilter(text: String) {
        if (!_searchArray.remove(text))
            _searchArray.add(text)
        _searchArrayFilter.postValue(_searchArray)
    }

    fun clearSearch() = _searchString.postValue("")
}