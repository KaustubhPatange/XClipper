package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Tag

interface TagRepository {

    fun insertTag(tag: Tag)

    fun deleteTag(tag: Tag)

    fun getAllData(): List<Tag>

    fun getAllLiveData(): LiveData<List<Tag>>
}