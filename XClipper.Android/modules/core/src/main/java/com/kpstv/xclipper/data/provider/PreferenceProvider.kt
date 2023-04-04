package com.kpstv.xclipper.data.provider

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceProvider {
    fun putStringKey(key: String, value: String?)
    fun getStringKey(key: String, default: String?): String?

    fun putEncryptString(key: String, value: String?)
    fun getEncryptString(key: String, default: String?) : String?

    fun getBooleanKey(key: String, default: Boolean): Boolean
    fun putBooleanKey(key: String, value: Boolean)

    fun putLongKey(key: String, value: Long)
    fun getLongKey(key: String, default: Long): Long

    fun putIntKey(key: String, value: Int)
    fun getIntKey(key: String, default: Int): Int

    fun putFloatKey(key: String, value: Float)
    fun getFloatKey(key: String, default: Float) : Float

    fun setStringSet(key: String, values: Set<String>)
    fun getStringSet(key: String, default: Set<String>): Set<String>

    fun removeKey(key: String)

    fun getAllKeys(): Set<String>

    fun observePreference(block: (SharedPreferences, String) -> Unit)

    fun observeBooleanKeyAsFlow(key: String, default: Boolean) : Flow<Boolean>
}