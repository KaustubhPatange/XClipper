package com.kpstv.xclipper.data.converters

import androidx.room.TypeConverter
import com.kpstv.xclipper.App.EMPTY_STRING
import com.kpstv.xclipper.App.ITEM_SEPARATOR
import com.kpstv.xclipper.App.PAIR_SEPARATOR

/**
 * Pair separator = :
 * Item separator = |
 **/

object TagConverter {

    private val TAG = javaClass.simpleName

    @TypeConverter
    @JvmStatic
    fun fromTagToString(pairs: Map<String, String>?): String? {
        pairs?.let {
            return it.flatMap { pair ->
                ArrayList<String>().apply {
                    add("${pair.key}${PAIR_SEPARATOR}${pair.value}")
                }
            }.joinToString(separator = ITEM_SEPARATOR) { data -> data }
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toTagFromString(data: String?): Map<String, String>? {
        data?.let {
            return it.split(ITEM_SEPARATOR).associate { string ->
                val pair = string.split(PAIR_SEPARATOR)
                if (!pair[0].isBlank())
                    Pair(pair[0], pair[1])
                else
                    Pair(EMPTY_STRING, EMPTY_STRING)
            }
        }
        return null
    }
}