package com.kpstv.xclipper.data.model

import com.kpstv.xclipper.extensions.GsonConverter

data class WebSettings(
    val useNewUpdater: Boolean = false
)

object WebSettingsConverter : GsonConverter<WebSettings>(WebSettings::class)