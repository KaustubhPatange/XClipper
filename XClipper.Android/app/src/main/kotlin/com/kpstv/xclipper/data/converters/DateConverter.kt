package com.kpstv.xclipper.data.converters

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import com.kpstv.xclipper.App.STANDARD_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object DateConverter {
    @TypeConverter
    @JvmStatic
    fun fromDateToString(date: Date?): String? {
        return if (date!=null) SimpleDateFormat(STANDARD_DATE_FORMAT)
            .format(date) else null
    }

    @TypeConverter
    @JvmStatic
    fun toDateFromString(dateString: String?): Date? {
        return if (dateString != null) SimpleDateFormat(STANDARD_DATE_FORMAT)
            .parse(dateString) else null
    }
}