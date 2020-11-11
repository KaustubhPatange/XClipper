package com.kpstv.xclipper.ui.viewmodels.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.App.EMPTY_STRING
import com.kpstv.xclipper.App.STAGGERED_SPAN_COUNT
import com.kpstv.xclipper.data.localized.dao.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Tag
import kotlin.collections.HashMap

class MainEditManager(
    tagRepository: TagDao
) {
    private val _spanCount = MutableLiveData(STAGGERED_SPAN_COUNT)
    private val _clip = MutableLiveData<Clip>()
    private val _tagFixedLiveData = MutableLiveData<List<Tag>>()
    private val _selectedTags = MutableLiveData<Map<String, String>>(HashMap())

    val tagFixedLiveData: LiveData<List<Tag>>
        get() = _tagFixedLiveData

    val selectedTags: LiveData<Map<String, String>>
        get() = _selectedTags

    val spanCount: LiveData<Int>
        get() = _spanCount

    /**
     * Important whenever want to edit the clip call this function at start
     */
    fun postClip(clip: Clip) {
        _clip.postValue(clip)
        updateTags(clip)
    }

    /**
     * Clear this
     */
    fun clearClip() {
        _clip.postValue(null)
        _selectedTags.postValue(null)
    }

    fun getSelectedTags() = _selectedTags.value

    fun getClip() = _clip.value

    fun addOrRemoveSelectedTag(tag: Tag) {
       val hashMap = if (_selectedTags.value != null)
           _selectedTags.value!!
        else
           HashMap()
        hashMap.toMutableMap().let {
            if (it.remove(tag.name) == null)
                it[tag.name] = EMPTY_STRING
            _selectedTags.postValue(it)
        }
    }

    fun postSpanCount(value: Int) {
        _spanCount.postValue(value)
    }

    /**
     * Call this function to update tags from the clip tags
     */
    private fun updateTags(clip: Clip) {
        _selectedTags.postValue(clip.tags?.filter {
            ClipTag.fromValue(it.key) == null
        })
    }
    private val TAG = javaClass.simpleName
    init {
        tagRepository.getAllLiveData().observeForever {
            _tagFixedLiveData.postValue(it.filter { tag ->
                ClipTag.fromValue(tag.name) == null
            })
        }
    }
}