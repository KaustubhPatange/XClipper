package com.kpstv.xclipper.data.model

import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@AutoGenerateListConverter(using = ConverterType.GSON)
data class Device (
    val id: String,
    val sdk: Int,
    val model: String
)