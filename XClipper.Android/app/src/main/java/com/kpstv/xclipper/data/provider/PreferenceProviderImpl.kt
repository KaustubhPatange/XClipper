package com.kpstv.xclipper.data.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.kpstv.license.Encryption.DecryptPref
import com.kpstv.license.Encryption.EncryptPref

class PreferenceProviderImpl(
    context: Context
) : PreferenceProvider {
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)

    private val _keyLiveData = MutableLiveData<String>()

    override fun putStringKey(key: String, value: String?) {
        preference.edit().apply {
            putString(key, value)
        }.apply()
        _keyLiveData.postValue(key)
    }

    override fun getStringKey(key: String, default: String?) =
        preference.getString(key, default)

    override fun putEncryptString(key: String, value: String?) {
        val data = if (value.isNullOrEmpty()) null else value.EncryptPref()
        preference.edit().apply {
            putString(key, data)
        }.apply()
        _keyLiveData.postValue(key)
    }

    override fun getEncryptString(key: String, default: String?): String? {
        val value = preference.getString(key, default)
        return if (value.isNullOrEmpty()) default else value.DecryptPref()
    }

    override fun getBooleanKey(key: String, default: Boolean) =
        preference.getBoolean(key, default)

    override fun putBooleanKey(key: String, value: Boolean) {
        preference.edit().apply {
            putBoolean(key, value)
        }.apply()
        _keyLiveData.postValue(key)
    }

    override fun removeKey(key: String) {
        preference.edit().apply {
            remove(key)
        }.apply()
    }

    override fun observePreference(block: (SharedPreferences, String) -> Unit) {
        _keyLiveData.observeForever {
            block.invoke(preference, it)
        }
    }
}