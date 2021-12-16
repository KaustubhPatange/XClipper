package com.kpstv.xclipper.ui.viewmodel.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainSearchManager @Inject constructor() {

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

    fun clearAllSearchFilter() {
        if (_searchArray.isNotEmpty()) {
            _searchArray.clear()
            _searchArrayFilter.postValue(_searchArray)
        }
    }

    fun existTagFilter(tag: Tag): Boolean {
        return _tagArray.any { it.name == tag.name }
    }

    fun addTagFilter(tag: Tag) {
        if (!_tagArray.any { it.name == tag.name }) {
            _tagArray.add(tag)
            _tagArrayFilter.postValue(_tagArray)
        }
    }

    fun removeTagFilter(tag: Tag) {
        _tagArray.removeAll { it.name == tag.name }
        _tagArrayFilter.postValue(_tagArray)
    }

    fun clearAllTagFilter() {
        if (_tagArray.isNotEmpty()) {
            _tagArray.clear()
            _tagArrayFilter.postValue(_tagArray)
        }
    }

    /**
     * Determines if any search filter is applied.
     */
    fun anyFilterApplied() =
        _searchArray.isNotEmpty() || _searchString.value?.isNotEmpty() == true || _tagArray.isNotEmpty()

    fun clearSearch() {
        if (_searchString.value != "") _searchString.postValue("")
    }

    fun clearAll() {
        clearSearch()
        clearAllSearchFilter()
        clearAllTagFilter()
    }
    private val TAG = javaClass.simpleName
}