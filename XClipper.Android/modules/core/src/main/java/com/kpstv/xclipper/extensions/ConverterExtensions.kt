package com.kpstv.xclipper.extensions

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kpstv.xclipper.extensions.utils.GsonUtils
import kotlin.reflect.KClass

abstract class GsonConverter<T : Any>(private val type: KClass<T>) {
    @TypeConverter
    fun toJson(model: T?): String? {
        if (model == null) return null
        val gson = Gson()
        return gson.toJson(model, type.java)
    }

    @TypeConverter
    fun fromJson(model: String?): T? {
        if (model == null) return null
        val gson = Gson()
        return gson.fromJson(model, type.java)
    }
}

abstract class GsonListConverter<T>(private val typeToken: TypeToken<List<T>>) {
    @TypeConverter
    fun toJsonFromList(model: List<T>?): String? {
        if (model == null) return null
        val gson = Gson()
        return gson.toJson(model, typeToken.type)
    }

    @TypeConverter
    fun fromJsonToList(model: String?): List<T>? {
        if (model == null) return null
        val gson = Gson()
        return gson.fromJson(model, typeToken.type)
    }
}