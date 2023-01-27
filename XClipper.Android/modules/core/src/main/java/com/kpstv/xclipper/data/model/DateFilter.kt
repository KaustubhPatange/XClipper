package com.kpstv.xclipper.data.model

import java.util.Date

data class DateFilter(val date: Date, val type: Type) {
    enum class Type {
        LESS_THAN,
        GREATER_THAN
    }
}