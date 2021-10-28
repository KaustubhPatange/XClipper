package com.kpstv.xclipper.ui.helpers.specials

import android.content.Context
import androidx.core.content.edit

internal class SpecialSettings(context: Context) {
    private val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun checkSetting(item: SpecialAction, value: Boolean) {
        preference.edit { putBoolean(item.name, value) }
    }

    fun getCheckSetting(item: SpecialAction) : Boolean {
        return preference.getBoolean(item.name, true)
    }

    fun getAllSetting() : List<SpecialAction> {
        return SpecialAction.all().filter { item ->
            preference.getBoolean(item.name, true)
        }
    }

    companion object {
        private const val PREF_NAME = "special_settings"
    }
}