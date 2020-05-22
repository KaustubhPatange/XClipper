package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.synthetic.main.dialog_create_tag.view.*

@Entity(tableName = "table_tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String
) {
    companion object {
        fun from(text: String): Tag =
            Tag(name = text)
    }
}