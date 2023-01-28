package com.kpstv.xclipper.data.model

data class TagFilter(val tags: List<Tag>, val type: Type = Type.CONTAINS) {
    enum class Type {
        CONTAINS,
        EXCLUDE
    }
}