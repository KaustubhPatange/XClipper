package com.kpstv.xclipper.data.localized.dao

import androidx.room.*
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.UserEntity

@Dao
interface UserEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: UserEntity)

    @Query("delete from ${UserEntity.TABLE_NAME}")
    suspend fun remove()

    @Transaction
    suspend fun update(entity: UserEntity) {
        remove()
        put(entity)
    }

    @Transaction
    suspend fun updateClips(clips: List<Clip>) {
        val user = get()
        user?.Clips = clips
        if (user != null) update(user)
    }

    @Transaction
    suspend fun updateDevices(devices: List<Device>) {
        val user = get()
        user?.Devices = devices
        if (user != null) update(user)
    }

    @Query("select IsLicensed from ${UserEntity.TABLE_NAME} limit 1")
    suspend fun isLicensed(): Boolean?

    @Query("select exists(select * from ${UserEntity.TABLE_NAME} limit 1)")
    suspend fun isExist(): Boolean

    @Query("select * from ${UserEntity.TABLE_NAME} limit 1")
    suspend fun get(): UserEntity?
}