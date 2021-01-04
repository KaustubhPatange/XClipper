package com.kpstv.xclipper.data.model

import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType

@AutoGenerateListConverter(using = ConverterType.GSON)
data class Dictionary<K, V>(val key: K, val value: V)