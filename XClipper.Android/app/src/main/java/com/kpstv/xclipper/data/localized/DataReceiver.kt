package com.kpstv.xclipper.data.localized

interface DataReceiver {
    fun<T> onDataReceive(value: T)
}