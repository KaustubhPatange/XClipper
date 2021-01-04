package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_tag")
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