package com.kpstv.xclipper.data.repository

import com.kpstv.xclipper.data.model.Definition
import com.kpstv.xclipper.extensions.listeners.ResponseListener

interface DefineRepository {
    fun insert(definition: Definition)
    suspend fun getData(word: String): Definition?
}