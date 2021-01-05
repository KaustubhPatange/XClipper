package com.kpstv.xclipper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType
import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.extensions.LicenseType
import com.kpstv.xclipper.extensions.mapToClass

data class User(
    val IsLicensed: Boolean,
    val TotalConnection: Int,
    val MaxItemStorage: Int,
    val LicenseStrategy: LicenseType,
    var Clips: List<Clip>?,
    var Devices: List<Device>?
) {
    companion object {
        fun parse(userDomain: UserDomain): User {
            return User(
                IsLicensed = userDomain.IsLicensed ?: false,
                TotalConnection = userDomain.TotalConnection ?: App.getMaxConnection(false),
                MaxItemStorage = userDomain.MaxItemStorage ?: App.getMaxStorage(false),
                LicenseStrategy = userDomain.LicenseStrategy ?: LicenseType.Invalid,
                Clips = userDomain.Clips?.filterNotNull(),
                Devices = userDomain.Devices?.filterNotNull()
            )
        }
        fun parse(userEntity: UserEntity): User = mapToClass(userEntity)
        fun from(json: String): User {
            return gson.fromJson(json, UserDomain::class.java).toUser()
        }
    }
}

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

data class UserDomain(
    val IsLicensed: Boolean?,
    val TotalConnection: Int?,
    val MaxItemStorage: Int?,
    val LicenseStrategy: LicenseType?,
    var Clips: List<Clip?>?,
    var Devices: List<Device?>?
)

fun UserDomain.toUser() = User.parse(this)