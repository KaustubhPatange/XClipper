package com.kpstv.xclipper.extensions.utils

import java.text.DecimalFormat

object SizeUtils {
    fun getSizePretty(size: Long?, addPrefix: Boolean = true): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        return if (size != null) {
            when {
                size < sizeMb -> df.format(size / sizeKb)
                    .toString() + if (addPrefix) " KB" else ""
                size < sizeGb -> df.format(
                    size / sizeMb
                ).toString() + " MB"
                size < sizeTerra -> df.format(size / sizeGb)
                    .toString() + if (addPrefix) " GB" else ""
                else -> ""
            }
        } else "0" + if (addPrefix) " B" else ""
    }
}