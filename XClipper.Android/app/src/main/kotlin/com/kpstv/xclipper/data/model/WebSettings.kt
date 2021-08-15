package com.kpstv.xclipper.data.model

import com.kpstv.bindings.AutoGenerateConverter
import com.kpstv.bindings.ConverterType

@AutoGenerateConverter(using = ConverterType.GSON)
data class WebSettings(
    val useNewUpdater: Boolean = false
)