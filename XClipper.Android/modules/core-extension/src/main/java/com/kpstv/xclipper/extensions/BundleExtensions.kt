package com.kpstv.xclipper.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun<reified T: Parcelable> Bundle.getParcelableExt(key: String?) : T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        getParcelable(key)
    }
}

@Suppress("DEPRECATION")
inline fun<reified T: Parcelable> Bundle.getParcelableArrayListExt(key: String?) : ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, T::class.java)
    } else {
        getParcelableArrayList(key)
    }
}