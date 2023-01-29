package com.kpstv.xclipper.ui.viewmodel.manager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.extensions.SaveRestore
import com.kpstv.xclipper.extensions.getParcelableArrayListExt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("NullSafeMutableLiveData")
@Singleton
class MainEditManager @Inject constructor(
    tagRepository: TagDao
) : SaveRestore {
    private val _spanCount = MutableLiveData(-1)
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
    fun setTagFromClip(clip: Clip) {
        _clip.postValue(clip)
        updateTags(clip)
    }

    /**
     * Clear this
     */
    fun clearClip() {
        _clip.postValue(null)
        _selectedTags.postValue(emptyList())
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
        val tags = clip.tags?.filter { tagMap ->
            val clipTag = ClipTag.fromValue(tagMap.key)
            clipTag == null || clipTag.isSpecialTag()
        } ?: emptyList()
        _selectedTags.postValue(tags)
    }
    private val TAG = javaClass.simpleName

    init {
        CoroutineScope(Dispatchers.IO).launch {
            tagRepository.getAllLiveData().collect { data ->
                val sortedList = data
                    .sortedByDescending { it.type.isSpecialTag() }
                    .filter { it.type.isUserTag() || it.type.isSpecialTag() }
                _tagFixedLiveData.postValue(sortedList)
            }
        }
    }

    override fun saveState(bundle: Bundle) {
        val out = Bundle().apply {
            _selectedTags.value?.let { putParcelableArrayList(KEY_SELECTED_TAGS, ArrayList(it)) }
            _clip.value?.let {
                putString(KEY_CLIP, it.toJson())
            }
        }
        bundle.putBundle(SAVE_KEY, out)
    }

    override fun restoreState(bundle: Bundle?) {
        bundle?.getBundle(SAVE_KEY)?.let { out ->
            out.getParcelableArrayListExt<ClipTagMap>(KEY_SELECTED_TAGS)?.let { _selectedTags.postValue(it) }
            out.getString(KEY_CLIP)?.let { _clip.value = Clip.fromJson(it) }
        }
    }

    private companion object {
        private const val EMPTY_STRING = ""

        private const val SAVE_KEY = "com.kpstv.xclipper:MainEditManager"
        private const val KEY_CLIP = "clip"
        private const val KEY_CLIP_TEXT = "clip"
        private const val KEY_SELECTED_TAGS = "selectedTags"
    }
}