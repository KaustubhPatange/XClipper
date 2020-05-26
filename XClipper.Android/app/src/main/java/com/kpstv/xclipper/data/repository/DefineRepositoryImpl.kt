package com.kpstv.xclipper.data.repository

import com.kpstv.xclipper.data.localized.DefineDao
import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefineRepositoryImpl(
    private val defineDao: DefineDao
) : DefineRepository {
    override fun insert(definition: Definition) {
        Coroutines.io {
            if (defineDao.getWord(definition.word!!) == null)
                defineDao.insert(definition)
        }
    }

    override suspend fun getData(word: String): Definition? {
        return withContext(Dispatchers.IO) {
            defineDao.getWord(word)
        }
    }
}