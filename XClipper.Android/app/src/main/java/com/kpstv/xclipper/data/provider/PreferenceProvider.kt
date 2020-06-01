package com.kpstv.xclipper.data.provider

import android.content.SharedPreferences

interface PreferenceProvider {
    fun putStringKey(key: String, value: String?)
    fun getStringKey(key: String, default: String?): String?

    fun getBooleanKey(key: String, default: Boolean): Boolean
    fun putBooleanKey(key: String, value: Boolean)

    fun observePreference(block: (SharedPreferences, String) -> Unit)

    /*fun getStringSetKey(key: String, value: Set<String>?)
    fun putStringSetKey(key: String, value: Set<String>?)*/
}