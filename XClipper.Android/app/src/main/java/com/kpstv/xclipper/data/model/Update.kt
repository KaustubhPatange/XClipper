package com.kpstv.xclipper.data.model

import com.google.gson.annotations.SerializedName

data class Update(
    @SerializedName("Mobile", alternate = ["mobile"])
    val mobile: Android
)

data class Android(
    @SerializedName("Version", alternate = ["version"])
    val version: String
)