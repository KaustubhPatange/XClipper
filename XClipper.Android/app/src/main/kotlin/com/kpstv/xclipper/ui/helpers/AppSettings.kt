package com.kpstv.xclipper.ui.helpers

import androidx.annotation.StringDef
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.IMPROVE_DETECTION
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor(
    private val preferenceProvider: PreferenceProvider
) {
    private val listeners = arrayListOf<Listener>()
    fun interface Listener {
        fun onChangeListener(@AppSettingKeys key: String, value: Any)
    }

    fun isImproveDetectionEnabled() : Boolean = preferenceProvider.getBooleanKey(IMPROVE_DETECTION, false)
    fun setImproveDetectionEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(IMPROVE_DETECTION, value)
        notifyListeners(IMPROVE_DETECTION, value)
    }

    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(key: String, value: Any) {
        listeners.forEach { it.onChangeListener(key, value) }
    }
}

@StringDef(IMPROVE_DETECTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AppSettingKeys {
    companion object Keys {
        const val IMPROVE_DETECTION = "improve_detection"
    }
}