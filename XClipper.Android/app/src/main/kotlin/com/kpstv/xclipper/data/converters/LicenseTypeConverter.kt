package com.kpstv.xclipper.data.converters

import androidx.room.TypeConverter
import com.kpstv.xclipper.extensions.enumerations.LicenseType

@Deprecated("AutoBindings automatically generates a type converter when used in a declaration")
object LicenseTypeConverter {
    @TypeConverter
    @JvmStatic
    fun fromLicenseType(t: LicenseType) = t.name

    @TypeConverter
    @JvmStatic
    fun toLicenseType(t: String) = LicenseType.valueOf(t)
}