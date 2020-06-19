package com.kpstv.xclipper.data.model

import com.google.gson.annotations.SerializedName

data class User (
    val IsLicensed: Boolean,
    val TotalConnection: Int,
    val MaxItemStorage: Int,
    var Clips: List<Clip>?,
    var Devices: List<Device>?
)