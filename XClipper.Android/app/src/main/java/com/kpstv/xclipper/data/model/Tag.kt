package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + name.hashCode()
        return result
    }

    companion object {
        fun from(text: String): Tag =
            Tag(name = text)
    }
}

data class TagMap(val name: String, var count: Int)