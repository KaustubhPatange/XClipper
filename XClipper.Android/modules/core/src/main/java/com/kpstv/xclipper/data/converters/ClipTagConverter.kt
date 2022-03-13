package com.kpstv.xclipper.data.converters

import androidx.room.TypeConverter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.extensions.keys
import com.kpstv.xclipper.extensions.values
import org.json.JSONObject

object ClipTagConverter {

    @TypeConverter
    @JvmStatic
    fun fromTagToString(pairs: List<ClipTagMap>?): String? {
        if (pairs == null) return null
        val jsonObject = JsonObject()
        for(key in pairs.keys().distinct()) {
            val array = JsonArray()
            pairs.filter { it.key == key }.values().forEach { array.add(it) }

            jsonObject.add(key, array)
        }
        return jsonObject.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toTagFromString(data: String?): List<ClipTagMap>? {
        if (data == null) return null
        val map = ArrayList<ClipTagMap>()
        val jsonObject = JSONObject(data)

        for(key in jsonObject.keys()) {
            val array = jsonObject.getJSONArray(key)
            for(i in 0 until array.length()) {
                map.add(ClipTagMap(key, array.getString(i)))
            }
        }
        return map
    }
}