package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_url")
data class UrlInfo(
    val longUrl: String,
    val shortUrl: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    companion object {
        fun from(longUrl: String, shortUrl: String): UrlInfo =
            UrlInfo(
                longUrl = longUrl,
                shortUrl = shortUrl
            )
    }
}