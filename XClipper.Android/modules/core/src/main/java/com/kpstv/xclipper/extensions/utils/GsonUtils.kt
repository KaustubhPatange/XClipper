package com.kpstv.xclipper.extensions.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.xclipper.data.converters.ClipDeserializer
import com.kpstv.xclipper.data.converters.DefinitionDeserializer
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Definition

object GsonUtils {
    fun get() : Gson {
        return GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Clip::class.java, ClipDeserializer())
            .registerTypeAdapter(Definition::class.java, DefinitionDeserializer())
            .create()
    }
}