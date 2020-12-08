package com.kpstv.xclipper.data.model

import com.kpstv.xclipper.App
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.extensions.LicenseType

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
        fun from(json: String): User {
            return gson.fromJson(json, UserDomain::class.java).toUser()
        }
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