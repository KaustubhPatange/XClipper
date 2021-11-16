package com.kpstv.xclipper.ui.helpers

import androidx.annotation.StringDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.CLIPBOARD_BLACKLIST_APPS
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.CLIPBOARD_SUGGESTIONS
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.CLIP_TEXT_TRIMMING
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.DATABASE_AUTO_SYNC
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.DATABASE_BINDING
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.DATABASE_DELETE_BINDING
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.IMAGE_MARKDOWN
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.IMPROVE_DETECTION
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.ON_BOARDING_SCREEN
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.POLICY_DISCLOSURE
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SHOW_SEARCH_FEATURE
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SWIPE_DELETE_CLIP_ITEM
import com.kpstv.xclipper.ui.helpers.AppSettings.Listener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
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

    fun isImproveDetectionEnabled(): Boolean =
        preferenceProvider.getBooleanKey(IMPROVE_DETECTION, false)

    fun setImproveDetectionEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(IMPROVE_DETECTION, value)
        notifyListeners(IMPROVE_DETECTION, value)
    }

    fun isDisclosureAgreementShown(): Boolean =
        preferenceProvider.getBooleanKey(POLICY_DISCLOSURE, false)

    fun setDisclosureAgreementShown(value: Boolean) {
        preferenceProvider.putBooleanKey(POLICY_DISCLOSURE, value)
        notifyListeners(POLICY_DISCLOSURE, value)
    }

    fun canRenderMarkdownImage(): Boolean = preferenceProvider.getBooleanKey(IMAGE_MARKDOWN, true)
    fun setRenderMarkdownImage(value: Boolean) {
        preferenceProvider.putBooleanKey(IMAGE_MARKDOWN, value)
        notifyListeners(IMAGE_MARKDOWN, value)
    }

    fun canShowClipboardSuggestions(): Boolean = preferenceProvider.getBooleanKey(CLIPBOARD_SUGGESTIONS, false)
    fun setShowClipboardSuggestions(value: Boolean) {
        preferenceProvider.putBooleanKey(CLIPBOARD_SUGGESTIONS, value)
        notifyListeners(CLIPBOARD_SUGGESTIONS, value)
    }

    fun isSwipeDeleteEnabledForClipItem(): Boolean = preferenceProvider.getBooleanKey(SWIPE_DELETE_CLIP_ITEM, true)
    fun setSwipeDeleteEnabledForClipItem(value: Boolean) {
        preferenceProvider.putBooleanKey(SWIPE_DELETE_CLIP_ITEM, value)
        notifyListeners(SWIPE_DELETE_CLIP_ITEM, value)
    }

    fun isTextTrimmingEnabled(): Boolean = preferenceProvider.getBooleanKey(CLIP_TEXT_TRIMMING, false)
    fun setTextTrimmingEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(CLIP_TEXT_TRIMMING, value)
        notifyListeners(CLIP_TEXT_TRIMMING, value)
    }

    fun isOnBoardingScreensShowed(): Boolean = preferenceProvider.getBooleanKey(ON_BOARDING_SCREEN, false)
    fun setOnBoardingScreensShowed(value: Boolean) {
        preferenceProvider.putBooleanKey(ON_BOARDING_SCREEN, value)
        notifyListeners(ON_BOARDING_SCREEN, value)
    }

    fun getClipboardMonitoringBlackListApps() : Set<String> = preferenceProvider.getStringSet(CLIPBOARD_BLACKLIST_APPS, emptySet())
    fun setClipboardMonitoringBlackListApps(value: Set<String>) {
        preferenceProvider.setStringSet(CLIPBOARD_BLACKLIST_APPS, value)
        notifyListeners(CLIPBOARD_BLACKLIST_APPS, value)
    }

    fun isBubbleOnBoardingDialogShown() : Boolean = preferenceProvider.getBooleanKey(SHOW_SEARCH_FEATURE, false)
    fun setBubbleOnBoardingDialogShown(value: Boolean) {
        preferenceProvider.putBooleanKey(SHOW_SEARCH_FEATURE, value)
        notifyListeners(SHOW_SEARCH_FEATURE, value)
    }

    fun isDatabaseAutoSyncEnabled() : Boolean = preferenceProvider.getBooleanKey(DATABASE_AUTO_SYNC, false)
    fun setDatabaseAutoSyncEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(DATABASE_AUTO_SYNC, value)
        notifyListeners(DATABASE_AUTO_SYNC, value)
    }

    fun isDatabaseBindingEnabled() : Boolean = preferenceProvider.getBooleanKey(DATABASE_BINDING, false)
    fun setDatabaseBindingEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(DATABASE_BINDING, value)
        notifyListeners(DATABASE_BINDING, value)
    }

    fun isDatabaseDeleteBindingEnabled() :  Boolean = preferenceProvider.getBooleanKey(DATABASE_DELETE_BINDING, false)
    fun setDatabaseDeleteBindingEnabled(value: Boolean) {
        preferenceProvider.putBooleanKey(DATABASE_DELETE_BINDING, value)
        notifyListeners(DATABASE_DELETE_BINDING, value)
    }

    /**
     * Observe the changes of the settings. The [default] value will emitted as soon as the [LiveData]
     * will start observing.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> observeChanges(@AppSettingKeys key: String, default: T): LiveData<T> = callbackFlow {
        val listener = Listener { localKey, value ->
            if (key == localKey) this.sendBlocking(value as T)
        }
        listeners.add(listener)
        sendBlocking(default)
        awaitClose { listeners.remove(listener) }
    }.asLiveData()

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

@StringDef(
    IMPROVE_DETECTION,
    POLICY_DISCLOSURE,
    IMAGE_MARKDOWN,
    CLIPBOARD_SUGGESTIONS,
    SWIPE_DELETE_CLIP_ITEM,
    CLIP_TEXT_TRIMMING,
    ON_BOARDING_SCREEN,
    CLIPBOARD_BLACKLIST_APPS,
    SHOW_SEARCH_FEATURE,
    DATABASE_AUTO_SYNC,
    DATABASE_BINDING,
    DATABASE_DELETE_BINDING
)
@Retention(AnnotationRetention.SOURCE)
annotation class AppSettingKeys {
    companion object Keys {
        const val IMPROVE_DETECTION = "improve_detection"
        const val POLICY_DISCLOSURE = "policy_disclosure"
        const val IMAGE_MARKDOWN = "render_image_markdown"
        const val CLIPBOARD_SUGGESTIONS = "clipboard_suggestions"
        const val SWIPE_DELETE_CLIP_ITEM = "swipe_delete_clip_item"
        const val CLIP_TEXT_TRIMMING = "clip_text_trimming"
        const val ON_BOARDING_SCREEN = "tutorial_pref"
        const val CLIPBOARD_BLACKLIST_APPS = "blacklist_pref"
        const val SHOW_SEARCH_FEATURE = "to_show_search_feature"
        const val DATABASE_AUTO_SYNC = "autoSync_pref"
        const val DATABASE_BINDING = "bind_pref"
        const val DATABASE_DELETE_BINDING = "bindDelete_pref"
    }
}