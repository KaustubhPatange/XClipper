package com.kpstv.xclipper.ui.helpers

import androidx.annotation.StringDef
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.IMAGE_MARKDOWN
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.IMPROVE_DETECTION
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.POLICY_DISCLOSURE
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

    fun isDisclosureAgreementShown() : Boolean = preferenceProvider.getBooleanKey(POLICY_DISCLOSURE, false)
    fun setDisclosureAgreementShown(value: Boolean) {
        preferenceProvider.putBooleanKey(POLICY_DISCLOSURE, value)
        notifyListeners(POLICY_DISCLOSURE, value)
    }

    fun canRenderMarkdownImage() : Boolean = preferenceProvider.getBooleanKey(IMAGE_MARKDOWN, true)
    fun setRenderMarkdownImage(value: Boolean) {
        preferenceProvider.putBooleanKey(IMAGE_MARKDOWN, value)
        notifyListeners(IMAGE_MARKDOWN, value)
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

@StringDef(IMPROVE_DETECTION, POLICY_DISCLOSURE, IMAGE_MARKDOWN)
@Retention(AnnotationRetention.SOURCE)
annotation class AppSettingKeys {
    companion object Keys {
        const val IMPROVE_DETECTION = "improve_detection"
        const val POLICY_DISCLOSURE = "policy_disclosure"
        const val IMAGE_MARKDOWN = "render_image_markdown"
    }
}