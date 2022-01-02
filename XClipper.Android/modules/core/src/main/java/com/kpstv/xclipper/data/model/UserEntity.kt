package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.xclipper.extensions.enumerations.LicenseType
import com.kpstv.xclipper.extensions.mapToClass

@Entity(tableName = UserEntity.TABLE_NAME)
@AutoGenerateConverter(using = ConverterType.GSON)
data class UserEntity (
    val IsLicensed: Boolean,
    val TotalConnection: Int,
    val MaxItemStorage: Int,
    val LicenseStrategy: LicenseType,
    var Clips: List<Clip>?,
    var Devices: List<Device>?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    companion object {
        fun from(user: User): UserEntity = mapToClass(user)
        const val TABLE_NAME = "table_current_user"
    }
}

fun UserEntity.toUser() : User = mapToClass(this)