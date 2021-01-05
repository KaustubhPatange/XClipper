package com.kpstv.xclipper.ui.viewmodels.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.App.EMPTY_STRING
import com.kpstv.xclipper.App.STAGGERED_SPAN_COUNT
import com.kpstv.xclipper.data.localized.dao.TagDao
import com.kpstv.xclipper.data.model.*
import com.kpstv.xclipper.extensions.ClipTagMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainEditManager @Inject constructor(
    tagRepository: TagDao
) {
    private val _spanCount = MutableLiveData(STAGGERED_SPAN_COUNT)
    private val _clip = MutableLiveData<Clip>()
    private val _tagFixedLiveData = MutableLiveData<List<Tag>>()
    private val _selectedTags = MutableLiveData<List<ClipTagMap>>(listOf())

    val tagFixedLiveData: LiveData<List<Tag>>
        get() = _tagFixedLiveData

    val selectedTags: LiveData<List<ClipTagMap>>
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
       val tagList: ArrayList<ClipTagMap> = if (_selectedTags.value != null)
           ArrayList(_selectedTags.value!!)
        else
           ArrayList()
        val element: ClipTagMap? = tagList.firstOrNull { it.key == tag.name }
        if (element == null)
            tagList.add(ClipTagMap(tag.name, EMPTY_STRING))
        else
            tagList.remove(element)
        _selectedTags.postValue(tagList)
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