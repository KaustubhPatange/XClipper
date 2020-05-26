package com.kpstv.xclipper.data.converters

import java.util.*

object DateFormatConverter {
    private val TAG = javaClass.simpleName

    @JvmStatic
    fun getFormattedDate(date: Date?): String {
        if (date == null) return "while ago"

        val todayDate = Calendar.getInstance().time

        val hours = (todayDate.time - date.time) / (1000 * 60 * 60)
        if (hours == 0L) return "while ago"
        if (hours == 1L) return "1 hour ago"
        if (hours < 24L) return "${hours.toInt()} hours ago"

        val days = hours / 24
        if (days == 1L) return "1 day ago"
        if (days < 7L) return "${days.toInt()} days ago"

        val weeks = days / 7
        if (weeks == 1L) return "1 week ago"
        if (weeks < 52) return "${weeks.toInt()} weeks ago"

        return "while ago"
    }
}