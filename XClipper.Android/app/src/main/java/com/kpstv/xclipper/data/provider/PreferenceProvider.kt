package com.kpstv.xclipper.data.provider

interface PreferenceProvider {
    fun putStringKey(key: String, value: String?)
    fun getStringKey(key: String, default: String?): String?

    fun getBooleanKey(key: String, default: Boolean): Boolean
    fun putBooleanKey(key: String, value: Boolean)

    /*fun getStringSetKey(key: String, value: Set<String>?)
    fun putStringSetKey(key: String, value: Set<String>?)*/
}