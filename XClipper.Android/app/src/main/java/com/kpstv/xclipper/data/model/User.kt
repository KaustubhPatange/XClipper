package com.kpstv.xclipper.data.model

import com.kpstv.xclipper.extensions.LicenseType

data class User(
    val IsLicensed: Boolean,
    val TotalConnection: Int,
    val MaxItemStorage: Int,
    val LicenseStrategy: LicenseType?,
    var Clips: List<Clip>?,
    var Devices: List<Device>?
)