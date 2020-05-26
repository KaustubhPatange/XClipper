package com.kpstv.xclipper.data.localized

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.xclipper.data.model.UrlInfo

@Dao
interface UrlDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(urlInfo: UrlInfo)

    @Query("select * from table_url where longUrl = :longUrl")
    fun getUrlInfo(longUrl: String): UrlInfo?
}