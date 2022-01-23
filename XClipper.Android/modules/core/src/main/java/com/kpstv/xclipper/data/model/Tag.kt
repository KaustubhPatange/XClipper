package com.kpstv.xclipper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@Entity(tableName = "table_tag")
@AutoGenerateConverter(using = ConverterType.GSON)
@AutoGenerateListConverter(using = ConverterType.GSON)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: ClipTagType
) {

    companion object {
        fun from(text: String, type: ClipTagType): Tag = Tag(name = text, type = type)
    }
}

data class TagMap(val name: String, var count: Int)