package com.kpstv.xclipper.data.converters

import android.util.Log
import androidx.room.TypeConverter
import com.kpstv.xclipper.App.ITEM_SEPARATOR
import com.kpstv.xclipper.App.PAIR_SEPARATOR
import com.kpstv.xclipper.data.model.ClipTag

/**
 * Pair separator = :
 * Item separator = |
 **/

object TagConverter {

    private val TAG = javaClass.simpleName

    @TypeConverter
    @JvmStatic
    fun fromTagToString(pairs: Map<ClipTag, String>?): String? {
        pairs?.let {
            return it.flatMap { pair ->
                ArrayList<String>().apply {
                    add("${pair.key.name}${PAIR_SEPARATOR}${pair.value}")
                }
            }.joinToString(separator = ITEM_SEPARATOR) { data -> data }
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toTagFromString(data: String?): Map<ClipTag, String>? {
        data?.let {
            return it.split(ITEM_SEPARATOR).associate { string ->
               val pair = string.split(PAIR_SEPARATOR)
                if (!pair[0].isNullOrBlank())
                    Pair(ClipTag.valueOf(pair[0]),pair[1])
                else
                    Pair(ClipTag.EMPTY,"")
            }
        }
        return null
    }
}