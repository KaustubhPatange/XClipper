package com.kpstv.xclipper.data.model

import android.os.Parcelable
import com.kpstv.bindings.AutoGenerateListConverter
import com.kpstv.bindings.ConverterType
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@AutoGenerateListConverter(using = ConverterType.GSON)
@Parcelize
data class Dictionary<K : Serializable, V : Serializable>(val key: K, val value: V) : Parcelable