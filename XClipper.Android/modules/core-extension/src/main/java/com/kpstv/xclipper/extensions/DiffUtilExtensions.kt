package com.kpstv.xclipper.extensions

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

fun <T> DiffUtil.ItemCallback<T>.asConfig(isBackground: Boolean = false): AsyncDifferConfig<T> {
    return AsyncDifferConfig.Builder(this).run {
        if (isBackground) {
            setBackgroundThreadExecutor(Dispatchers.IO.asExecutor())
        }
        build()
    }
}