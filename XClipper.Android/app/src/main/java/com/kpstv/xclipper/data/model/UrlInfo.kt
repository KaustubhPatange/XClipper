package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_url")
data class UrlInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val longUrl: String,
    val shortUrl: String
) {
    companion object {
        fun from(longUrl: String, shortUrl: String) =
            UrlInfo(
                longUrl = longUrl,
                shortUrl = shortUrl
            )
    }
}