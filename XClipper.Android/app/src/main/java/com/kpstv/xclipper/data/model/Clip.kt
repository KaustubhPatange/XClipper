package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonElement
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.extensions.enumValueOrNull
import org.json.JSONObject
import java.util.*

@Entity(tableName = "table_clip")
data class Clip(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val data: String?,
    val time: Date?,
    var tags: Map<String, String>? = null
) {
    var toDisplay = false
    var timeString = "while ago"

    companion object {
        /**
         * Generates Clip data along with the properties "data", "time"
         */
        fun from(json: String): Clip = with(JSONObject(json)) {
            Clip(
                data = this["data"].toString(),
                time = DateConverter.toDateFromString(this["time"].toString())
            )
        }

        /**
         * Generates Clip with the properties "data", "time", "tags"
         */
        fun from(unencryptedData: String, tags: Map<String, String>?) =
            Clip(
                data = unencryptedData.Encrypt(),
                time = Calendar.getInstance().time,
                tags = tags
            )

        /**
         * Generates Clip data along with the properties "data", "time"
         */
        fun from(json: JsonElement): Clip = from(json.toString())

        /**
         * A function which generates "timeString" property from "time"
         */
        fun autoFill(clip: Clip) = with(clip) {
            clip.timeString = DateFormatConverter.getFormattedDate(time)
        }
    }
}

data class ClipEntry(
    val data: String?,
    val time: String?
) {
    companion object {
        fun from(clip: Clip): ClipEntry = with(clip) {
            ClipEntry(data, DateConverter.fromDateToString(time))
        }

    }
}

enum class ClipTag {
    PHONE, DATE, URL, EMAIL, EMPTY;

    companion object {
        fun fromValue(text: String) =
            enumValueOrNull<ClipTag>(text.toUpperCase(Locale.ROOT))
    }
}