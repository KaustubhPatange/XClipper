package com.kpstv.xclipper.ui.viewmodel.manager

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extension.enumeration.SpecialTagFilter
import com.kpstv.xclipper.extensions.SaveRestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainSearchManager @Inject constructor() : SaveRestore {

    private val _searchString = MutableLiveData("")
    private val _searchArrayFilter = MutableLiveData<List<String>>(emptyList())
    private val _tagArrayFilter = MutableLiveData<List<Tag>>(emptyList())
    private val _specialTagFilter = MutableLiveData<List<SpecialTagFilter>>(emptyList())

    val searchString: LiveData<String>
        get() = _searchString

    val tagFilters: LiveData<List<Tag>>
        get() = _tagArrayFilter

    val specialTagFilters: LiveData<List<SpecialTagFilter>>
        get() = _specialTagFilter

    val searchFilters: LiveData<List<String>>
        get() = _searchArrayFilter

    /* Search Filters */

    fun setSearchText(text: String) {
        _searchString.postValue(text)
    }

    fun addOrRemoveSearchFilter(text: String) {
        val searchFilters = _searchArrayFilter.value!!

        if (searchFilters.contains(text)) {
            _searchArrayFilter.value = searchFilters.filterNot { it == text }
        } else {
            _searchArrayFilter.value = searchFilters + text
        }
    }

    fun clearAllSearchFilter() {
        val searchFilters = _searchArrayFilter.value!!
        if (searchFilters.isNotEmpty()) {
            _searchArrayFilter.value = emptyList()
        }
    }

    /* Tag Filters */

    fun existTagFilter(tag: Tag): Boolean {
        val tagFilters = _tagArrayFilter.value!!
        return tagFilters.any { it.name == tag.name }
    }

    fun addTagFilter(tag: Tag) {
        val tagFilters = _tagArrayFilter.value!!
        if (tagFilters.none { it.name == tag.name }) {
            _tagArrayFilter.value = tagFilters + tag
        }
    }

    fun removeTagFilter(tag: Tag) {
        val tagFilters = _tagArrayFilter.value!!
        _tagArrayFilter.value = tagFilters.filterNot { it.name == tag.name }
    }

    fun clearAllTagFilter() {
        val tagFilters = _tagArrayFilter.value!!
        if (tagFilters.isNotEmpty()) {
            _tagArrayFilter.value = emptyList()
        }
    }

    /* Special Tag Filters */

    fun existSpecialTagFilter(tag: SpecialTagFilter): Boolean {
        val filters = _specialTagFilter.value!!
        return filters.any { it.name == tag.name }
    }

    fun addSpecialTagFilter(tag: SpecialTagFilter) {
        val filters = _specialTagFilter.value!!
        if (filters.none { it.name == tag.name }) {
            _specialTagFilter.value = filters + tag
        }
    }

    fun removeSpecialTagFilter(tag: SpecialTagFilter) {
        val filters = _specialTagFilter.value!!
        _specialTagFilter.value = filters.filterNot { it.name == tag.name }
    }

    fun clearAllSpecialTag() {
        val filters = _specialTagFilter.value!!
        if (filters.isNotEmpty()) {
            _specialTagFilter.value = emptyList()
        }
    }

    /**
     * Determines if any search filter is applied.
     */
    fun anyFilterApplied() : Boolean {
        val searchString = _searchString.value!!
        val searchFilters = _searchArrayFilter.value!!
        val tagFilters = _tagArrayFilter.value!!
        val specialTagFilter = _specialTagFilter.value!!

        return searchFilters.isNotEmpty() || searchString.isNotEmpty() || tagFilters.isNotEmpty() || specialTagFilter.isNotEmpty()
    }


    fun clearSearch() {
        if (_searchString.value != "") _searchString.postValue("")
    }

    fun clearAll() {
        clearSearch()
        clearAllSearchFilter()
        clearAllTagFilter()
        clearAllSpecialTag()
    }

    override fun saveState(bundle: Bundle) {
        val out = Bundle().apply {
            _searchArrayFilter.value?.let { putStringArrayList(KEY_SEARCH_ARRAY, ArrayList(it)) }
            _tagArrayFilter.value?.let { putParcelableArrayList(KEY_TAG_ARRAY, ArrayList(it)) }
            _specialTagFilter.value?.let { putParcelableArrayList(KEY_SPECIAL_ARRAY, ArrayList(it)) }
        }
        bundle.putBundle(SAVE_KEY, out)
    }

    override fun restoreState(bundle: Bundle?) {
        bundle?.getBundle(SAVE_KEY)?.let { out ->
            out.getStringArrayList(KEY_SEARCH_ARRAY)?.let { _searchArrayFilter.postValue(it) }
            out.getParcelableArrayList<Tag>(KEY_TAG_ARRAY)?.let { _tagArrayFilter.postValue(it) }
            out.getParcelableArrayList<SpecialTagFilter>(KEY_SPECIAL_ARRAY)?.let { _specialTagFilter.postValue(it) }
        }
    }

    private companion object {
        private const val SAVE_KEY = "com.kpstv.xclipper:MainSearchManager"

        private const val KEY_SEARCH_ARRAY = "searchArray"
        private const val KEY_TAG_ARRAY = "tagArray"
        private const val KEY_SPECIAL_ARRAY = "specialArray"
    }
    private val TAG = javaClass.simpleName
}