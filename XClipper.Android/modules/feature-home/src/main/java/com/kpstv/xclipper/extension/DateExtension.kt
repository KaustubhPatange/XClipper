package com.kpstv.xclipper.extension

import java.text.SimpleDateFormat
import java.util.*

private const val STANDARD_DATE_FORMAT = "yyyyMMddHHmmss"
fun Date.getFormattedDate(): String = SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.US).format(this)