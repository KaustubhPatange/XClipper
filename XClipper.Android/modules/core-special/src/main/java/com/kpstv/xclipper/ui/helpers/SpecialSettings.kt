package com.kpstv.xclipper.ui.helpers

import android.content.Context
import androidx.core.content.edit
import com.kpstv.xclipper.extensions.SpecialAction


// TODO: Make internal later
class SpecialSettings(context: Context) {
    private val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun checkSetting(item: SpecialAction, value: Boolean) {
        preference.edit { putBoolean(item.name, value) }
    }

    fun getCheckSetting(item: SpecialAction) : Boolean {
        return preference.getBoolean(item.name, true)
    }

    fun getDictionaryLang() : String = preference.getString(DICTIONARY_LANG, DictionaryLanguage) ?: DictionaryLanguage
    fun setDictionaryLang(value: String) {
        preference.edit { putString(DICTIONARY_LANG, value) }
    }

    fun getAllSetting() : List<SpecialAction> {
        return SpecialAction.all().filter { item ->
            preference.getBoolean(item.name, true)
        }
    }

    companion object {
        private const val PREF_NAME = "special_settings"
        private const val DICTIONARY_LANG = "dictionary_lang"

        private const val DictionaryLanguage = "en"
    }
}
