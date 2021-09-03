package com.kpstv.xclipper.data.provider

import android.content.SharedPreferences

interface PreferenceProvider {
    fun putStringKey(key: String, value: String?)
    fun getStringKey(key: String, default: String?): String?

    fun putEncryptString(key: String, value: String?)
    fun getEncryptString(key: String, default: String?) : String?

    fun getBooleanKey(key: String, default: Boolean): Boolean
    fun putBooleanKey(key: String, value: Boolean)

    fun putLongKey(key: String, value: Long)
    fun getLongKey(key: String, default: Long): Long

    fun setStringSet(key: String, values: Set<String>)
    fun getStringSet(key: String, default: Set<String>): Set<String>

    fun removeKey(key: String)

    fun observePreference(block: (SharedPreferences, String) -> Unit)
}