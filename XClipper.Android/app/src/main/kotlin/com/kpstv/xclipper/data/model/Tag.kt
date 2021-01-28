package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@Entity(tableName = "table_tag")
@AutoGenerateListConverter(using = ConverterType.GSON)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String
) {

    companion object {
        fun from(text: String): Tag = Tag(name = text)
    }
}

data class TagMap(val name: String, var count: Int)