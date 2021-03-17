package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.xclipper.data.model.Preview.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class Preview(
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val url: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    companion object {
        const val TABLE_NAME = "table_preview"
    }
}