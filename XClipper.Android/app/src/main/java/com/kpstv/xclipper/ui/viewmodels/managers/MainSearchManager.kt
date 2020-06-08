package com.kpstv.xclipper.ui.viewmodels.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.model.Tag

class MainSearchManager {

    private var _searchString = MutableLiveData("")
    private var _searchArray = ArrayList<String>()
    private var _tagArray = ArrayList<Tag>()
    private var _searchArrayFilter = MutableLiveData(_searchArray)
    private var _tagArrayFilter = MutableLiveData(_tagArray)

    val searchString: LiveData<String>
        get() = _searchString

    val tagFilters: LiveData<ArrayList<Tag>>
        get() = _tagArrayFilter

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

    fun addTagFilter(tag: Tag) {
        if (!_tagArray.contains(tag)) {
            _tagArray.add(tag)
            _tagArrayFilter.postValue(_tagArray)
        }
    }

    fun removeTagFilter(tag: Tag) {
        _tagArray.remove(tag)
        _tagArrayFilter.postValue(_tagArray)
    }

    fun clearSearch() = _searchString.postValue("")
    private val TAG = javaClass.simpleName
}