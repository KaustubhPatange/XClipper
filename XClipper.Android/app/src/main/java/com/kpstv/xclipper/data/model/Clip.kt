package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.JsonElement
import com.kpstv.xclipper.data.converters.DateConverter
import org.json.JSONObject
import java.util.*

@Entity(tableName = "table_clip")
data class Clip (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val data: String?,
    val time: Date?
) {
    companion object {
        fun from(json: String): Clip  = with(JSONObject(json)) {
            Clip(data = this["data"].toString(), time = DateConverter.toDateFromString(this["time"].toString()))
        }
        fun from(json: JsonElement): Clip = from(json.toString())
    }
}

data class ClipEntry (
    val data: String?,
    val time: String?
) {
    companion object {
        fun from(clip: Clip) : ClipEntry = with(clip) {
            ClipEntry(data, DateConverter.fromDateToString(time))
        }
    }
}