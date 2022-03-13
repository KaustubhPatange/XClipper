package com.kpstv.xclipper.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Dictionary<K : Serializable, V : Serializable>(val key: K, val value: V) : Parcelable