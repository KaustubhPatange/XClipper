package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonElement
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.extensions.enumValueOrNull
import com.kpstv.xclipper.extensions.utils.ClipUtils
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

@Entity(tableName = "table_clip")
@AutoGenerateListConverter(using = ConverterType.GSON)
data class Clip(
    val data: String,
    val time: Date,
    var isPinned: Boolean = false, // TODO: If possible make it val?
    var tags: Map<String, String>? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var toDisplay = false // TODO: Do you need this?
    var timeString = "while ago"

    companion object {
        /**
         * Generates Clip data along with the properties "data", "time"
         */
        fun parse(json: String): Clip? = with(JSONObject(json)) {
            if (!has("data")) return null
            if (!has("time")) return null
            return Clip(
                data = this["data"].toString() ,
                time = DateConverter.toDateFromString(this["time"].toString())!!
            )
        }

        /**
         * This will update the clip with new time & tags.
         */
        fun from(clip: Clip): Clip {
            val tagMap: Map<String, String> = clip.tags ?: HashMap()
            return clip.copy(
                time = Calendar.getInstance().time,
                tags = tagMap + ClipUtils.determineTags(clip.data)
            ).also { it.id = clip.id }
        }

        fun from(unencryptedData: String): Clip {
            return from(unencryptedData, null)
        }

        /**
         * Generates Clip with the properties "data", "time", "tags"
         */
        fun from(unencryptedData: String, tags: Map<String, String>?): Clip  {
            val tagMap = tags ?: HashMap()
            return Clip(
                data = unencryptedData,
                time = Calendar.getInstance().time,
                isPinned = false,
                tags = tagMap + ClipUtils.determineTags(unencryptedData)
            )
        }


        /**
         * Generates Clip data along with the properties "data", "time"
         */
        fun parse(json: JsonElement): Clip? = parse(json.toString())

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
    PHONE, DATE, URL, EMAIL, MAP;

    companion object {
        fun fromValue(text: String) =
            enumValueOrNull<ClipTag>(text.toUpperCase(Locale.ROOT))
    }
}