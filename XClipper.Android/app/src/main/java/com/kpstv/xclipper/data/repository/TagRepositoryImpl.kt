package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.localized.TagDao
import com.kpstv.xclipper.data.model.Tag
import com.kpstv.xclipper.extensions.Coroutines

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {

    override fun insertTag(tag: Tag) {
        Coroutines.io {
            tagDao.insert(tag)
        }
    }

    override fun deleteTag(tag: Tag) {
        Coroutines.io {
            tagDao.delete(tag)
        }
    }

    override fun getAllData(): List<Tag> = tagDao.getAllData()

    override fun getAllLiveData(): LiveData<List<Tag>> = tagDao.getAllLiveData()
}