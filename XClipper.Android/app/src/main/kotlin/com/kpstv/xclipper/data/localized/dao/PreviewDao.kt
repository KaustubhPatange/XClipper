package com.kpstv.xclipper.data.localized.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kpstv.xclipper.data.model.Preview

@Dao
interface PreviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: Preview)

    @Query("select * from ${Preview.TABLE_NAME} where url = :url limit 1")
    suspend fun getFromUrl(url: String): Preview?
}