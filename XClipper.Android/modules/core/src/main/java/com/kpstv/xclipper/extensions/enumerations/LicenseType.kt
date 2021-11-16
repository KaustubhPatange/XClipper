package com.kpstv.xclipper.extensions.enumerations

import com.google.gson.annotations.SerializedName

enum class LicenseType {
    @SerializedName("0")
    Standard,
    @SerializedName("1")
    Premium,
    @SerializedName("2")
    Invalid
}
