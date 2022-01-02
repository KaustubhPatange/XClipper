package com.kpstv.xclipper.data.model

import com.kpstv.xclipper.extensions.enumerations.LicenseType

data class UserDomain(
    val IsLicensed: Boolean?,
    val TotalConnection: Int?,
    val MaxItemStorage: Int?,
    val LicenseStrategy: LicenseType?,
    var Clips: List<Clip?>?,
    var Devices: List<Device?>?
)