package com.kpstv.xclipper.data.localized.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.xclipper.data.model.UrlInfo

@Dao
interface UrlDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(urlInfo: UrlInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(urlInfos: List<UrlInfo>)

    @Query("select * from table_url where longUrl = :longUrl")
    suspend fun getUrlInfo(longUrl: String): UrlInfo?

    @Query("select * from table_url")
    suspend fun getAllData(): List<UrlInfo>

    @Query("delete from table_url")
    suspend fun deleteAll()
}