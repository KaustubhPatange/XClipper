package com.kpstv.xclipper.extensions.listeners

interface DataReceiver {
    fun<T> onDataReceive(value: T)
}