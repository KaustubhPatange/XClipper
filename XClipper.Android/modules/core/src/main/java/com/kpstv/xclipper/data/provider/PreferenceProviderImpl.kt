package com.kpstv.xclipper.data.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.kpstv.license.Encryption.DecryptPref
import com.kpstv.license.Encryption.EncryptPref
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PreferenceProviderImpl @Inject constructor(
    @ApplicationContext context: Context
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

    override fun putLongKey(key: String, value: Long) {
        preference.edit {
            putLong(key, value)
        }
    }

    override fun getLongKey(key: String, default: Long) =
        preference.getLong(key, default)

    override fun removeKey(key: String) {
        preference.edit().apply {
            remove(key)
        }.apply()
    }

    override fun getStringSet(key: String, default: Set<String>): Set<String> {
        return preference.getStringSet(key, default) ?: default
    }

    override fun setStringSet(key: String, values: Set<String>) {
        preference.edit {
            putStringSet(key, values)
        }
    }

    override fun putIntKey(key: String, value: Int) {
        preference.edit { putInt(key, value) }
    }

    override fun getIntKey(key: String, default: Int): Int = preference.getInt(key, default)

    override fun putFloatKey(key: String, value: Float) {
        preference.edit { putFloat(key, value) }
    }

    override fun getFloatKey(key: String, default: Float) = preference.getFloat(key, default)

    override fun getAllKeys(): Set<String> {
        return preference.all.keys.toSet()
    }

    override fun observePreference(block: (SharedPreferences, String) -> Unit) {
        _keyLiveData.observeForever {
            block.invoke(preference, it)
        }
    }

    override fun observeBooleanKeyAsFlow(key: String, default: Boolean): Flow<Boolean> = callbackFlow {
        fun sendUpdatedValue() {
            val value = preference.getBoolean(key, default)
            trySendBlocking(value)
        }
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (key == prefKey) sendUpdatedValue()
        }
        preference.registerOnSharedPreferenceChangeListener(listener)
        sendUpdatedValue()

        awaitClose { preference.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}