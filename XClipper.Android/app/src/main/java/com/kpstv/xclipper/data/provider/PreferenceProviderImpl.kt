package com.kpstv.xclipper.data.provider

import android.content.Context
import androidx.preference.PreferenceManager
import com.kpstv.xclipper.App.EMPTY_STRING

class PreferenceProviderImpl(
    context: Context
) : PreferenceProvider {

    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    override fun putStringKey(key: String, value: String?) =
        preference.edit().apply {
            putString(key, value)
        }.apply()

    override fun getStringKey(key: String, default: String?) =
        preference.getString(key, default)

    override fun getBooleanKey(key: String, default: Boolean) =
        preference.getBoolean(key, default)

    override fun putBooleanKey(key: String, value: Boolean) =
        preference.edit().apply {
            putBoolean(key, value)
        }.apply()
}