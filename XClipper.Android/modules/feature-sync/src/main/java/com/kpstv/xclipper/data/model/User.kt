package com.kpstv.xclipper.data.model

import com.kpstv.xclipper.extensions.enumerations.LicenseType
import com.kpstv.xclipper.extensions.utils.GsonUtils
import com.kpstv.xclipper.extensions.utils.SyncUtils

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
                TotalConnection = userDomain.TotalConnection ?: SyncUtils.getMaxConnection(false),
                MaxItemStorage = userDomain.MaxItemStorage ?: SyncUtils.getMaxStorage(false),
                LicenseStrategy = userDomain.LicenseStrategy ?: LicenseType.Invalid,
                Clips = userDomain.Clips?.filterNotNull(),
                Devices = userDomain.Devices?.filterNotNull()
            )
        }
        fun from(json: String): User {
            return GsonUtils.get().fromJson(json, UserDomain::class.java).toUser()
        }
    }
}

fun UserDomain.toUser(): User = User.parse(this)