package com.kpstv.xclipper.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonElement
import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.xclipper.data.converters.DateConverter
import com.kpstv.xclipper.data.converters.DateFormatConverter
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.extensions.Logger
import com.kpstv.xclipper.extensions.utils.ClipUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "table_clip")
@AutoGenerateConverter(using = ConverterType.GSON)
@AutoGenerateListConverter(using = ConverterType.GSON)
data class Clip(
    val data: String,
    val time: Date,
    val isPinned: Boolean = false,
    var tags: List<ClipTagMap>? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var timeString = "while ago"

    fun copyWithFields(data: String = this.data, time: Date = this.time, isPinned: Boolean = this.isPinned, tags: List<ClipTagMap>? = this.tags) : Clip {
        return copy(data = data, time = time, isPinned = isPinned, tags = tags).apply {
            id = this@Clip.id
            timeString = this@Clip.timeString
        }
    }

    fun getFullFormattedDate(): String {
        return try {
            SimpleDateFormat(FULL_DATA_FORMAT, Locale.getDefault()).format(time)
        } catch (e: Exception) {
            Logger.w(e, "Incorrect date time format: $time")
            "unknown"
        }
    }

    fun updateTime(): Clip = copyWithFields(time = Calendar.getInstance().time)

    fun toJson(): String = ClipConverter.toStringFromClip(this)!!

    companion object {
        private const val FULL_DATA_FORMAT = "dd MMM yyyy, hh:mm a"

        fun fromJson(model: String): Clip = ClipConverter.fromStringToClip(model)!!

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
            val tagList: List<ClipTagMap> = clip.tags ?: listOf()
            return clip.copy(
                time = Calendar.getInstance().time,
                tags = tagList + ClipUtils.determineTags(clip.data)
            ).also { it.id = clip.id }
        }

        fun from(unencryptedData: String): Clip {
            return from(unencryptedData, null)
        }

        /**
         * Generates Clip with the properties "data", "time", "tags".
         */
        fun from(unencryptedData: String, tags: List<ClipTagMap>?): Clip {
            val tagMap = tags ?: listOf()
            return Clip(
                data = unencryptedData,
                time = Calendar.getInstance().time,
                isPinned = false,
                tags = tagMap + ClipUtils.determineTags(unencryptedData)
            )
        }

        /**
         * Merges multiple [clips] into a single one.
         */
        fun from(clips: List<Clip>) : Clip {
            val data = clips.joinToString(separator = "\n") { it.data }
            val tagMap = clips.flatMap { it.tags ?: listOf() }.distinctBy { it.value }
            val isPinned = clips.any { it.isPinned }
            return Clip(
                data = data,
                time = Calendar.getInstance().time,
                isPinned = isPinned,
                tags = tagMap
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

data class PartialClipTagMap(
    val id: Int,
    @ColumnInfo(name = "tags")
    val items: List<ClipTagMap> // make sure it's not empty when checked from query.
)

/*
 * Marker indicates whether a tag is a system special tag (eg: LOCK) managed completely by XClipper &
 * has behavior different to the one corresponds to non special tag (eg: PHONE).
 *
 * 0 -> Non special tag
 * 1 -> Special tag
 */
enum class ClipTag(val marker: Int) {
    LOCK(1), PHONE(0), DATE(0), URL(0), EMAIL(0), MAP(0);

    fun isSystemTag() : Boolean = marker == 0
    fun isSpecialTag() : Boolean = marker == 1

    companion object {
        fun fromValue(text: String) = getValueOrNull<ClipTag>(text.uppercase(Locale.ROOT))
    }
}

enum class ClipTagType {
    /**
     * System tags are phone, date, url that are automatically assigned based on text recognition.
     */
    SYSTEM_TAG,

    /**
     * Special tags are "lock" that have a behavior different than the other tags are managed &
     * controlled by the app.
     */
    SPECIAL_TAG,

    /**
     * These are created by user.
     */
    USER_TAG;

    fun isSystemTag() : Boolean = this == SYSTEM_TAG
    fun isSpecialTag() : Boolean = this == SPECIAL_TAG
    fun isUserTag() : Boolean = this == USER_TAG
}

private inline fun <reified T : Enum<T>> getValueOrNull(name: String): T? {
    return enumValues<T>().find { it.name == name }
}