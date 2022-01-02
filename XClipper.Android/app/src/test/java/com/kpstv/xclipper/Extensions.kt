package com.kpstv.xclipper

fun String.toLines(): List<String> {
    return this.split("[\n|\r]".toRegex())
}
