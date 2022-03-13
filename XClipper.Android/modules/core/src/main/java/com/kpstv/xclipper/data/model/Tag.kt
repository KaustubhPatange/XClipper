package com.kpstv.xclipper.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "table_tag")
@Parcelize
data class Tag(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: ClipTagType
) : Parcelable {

    fun getClipTag() : ClipTag? = ClipTag.fromValue(name)
    companion object {
        fun from(text: String, type: ClipTagType): Tag = Tag(name = text, type = type)
    }
}

data class TagMap(val name: String, var count: Int)