package com.kpstv.xclipper.extensions

import java.text.SimpleDateFormat
import java.util.*

enum class DatePattern(val string: String) {
  TIMESTAMP("yyyyMMddHHmmss")
}

fun Date.getFormattedDate(pattern: DatePattern = DatePattern.TIMESTAMP): String = SimpleDateFormat(pattern.string, Locale.US).format(this)