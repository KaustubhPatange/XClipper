package com.kpstv.xclipper.extensions.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.xclipper.data.converters.ClipDeserializer
import com.kpstv.xclipper.data.model.Clip
import java.lang.reflect.Type

object GsonUtils {
    fun get(vararg typeAdapters: Pair<Type, Any>) : Gson {
        return GsonBuilder().run {
            serializeNulls()
            registerTypeAdapter(Clip::class.java, ClipDeserializer())
            typeAdapters.forEach { registerTypeAdapter(it.first, it.second) }
            create()
        }
    }
}