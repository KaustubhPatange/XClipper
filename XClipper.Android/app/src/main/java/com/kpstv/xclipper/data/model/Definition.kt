package com.kpstv.xclipper.data.model

data class Definition(
    val word: String?,
    val define: String?
) {
    companion object {
        fun returnNull() = Definition(null,null)
    }
}