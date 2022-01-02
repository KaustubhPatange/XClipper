package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@Entity(tableName = "table_url")
@AutoGenerateListConverter(using = ConverterType.GSON)
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