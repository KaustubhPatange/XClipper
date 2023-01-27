package com.kpstv.xclipper.ui.helpers

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import androidx.annotation.StringDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.getRawDataAttr
import com.kpstv.xclipper.extensions.small
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_DATA_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_DAY_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_EXCLUDE_TAGS_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_PINNED_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.AUTO_DELETE_REMOTE_PREF
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.CLIPBOARD_BLACKLIST_APPS
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.CLIPBOARD_CLEAR_PREF
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
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SUGGESTION_BUBBLE_COORDINATES
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SUGGESTION_BUBBLE_X_GRAVITY
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SUGGESTION_BUBBLE_Y_POS
import com.kpstv.xclipper.ui.helpers.AppSettingKeys.Keys.SWIPE_DELETE_CLIP_ITEM
import com.kpstv.xclipper.ui.helpers.AppSettings.Listener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext private val context: Context,
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

    fun isClipboardClearEnabled(): Boolean = preferenceProvider.getBooleanKey(CLIPBOARD_CLEAR_PREF, false)

    /**
     * Pair.first is horizontal [Gravity] & Pair.second is y offset.
     */
    fun getSuggestionBubbleCoordinates(): Pair<Int, Float> {
        val actionBarSize = context.getRawDataAttr(android.R.attr.actionBarSize).run {
            TypedValue.complexToDimensionPixelSize(this, context.resources.displayMetrics)
        }
        val gravity = preferenceProvider.getIntKey(SUGGESTION_BUBBLE_X_GRAVITY, Gravity.TOP or Gravity.START)
        val yOffset = preferenceProvider.getFloatKey(SUGGESTION_BUBBLE_Y_POS, actionBarSize.toFloat())
        return gravity to yOffset
    }
    fun setSuggestionBubbleCoordinates(gravity: Int, yOffset: Float) {
        preferenceProvider.putIntKey(SUGGESTION_BUBBLE_X_GRAVITY, gravity)
        preferenceProvider.putFloatKey(SUGGESTION_BUBBLE_Y_POS, yOffset)
        notifyListeners(SUGGESTION_BUBBLE_COORDINATES, (gravity to yOffset))
    }

    fun canAutoDeleteClips(): Boolean = preferenceProvider.getBooleanKey(AUTO_DELETE_PREF, false)
    fun setAutoDeleteClips(value: Boolean) {
        preferenceProvider.putBooleanKey(AUTO_DELETE_PREF, value)
        notifyListeners(AUTO_DELETE_PREF, value)
    }

    fun getAutoDeleteSetting(): AutoDeleteSetting = AutoDeleteSetting(
        shouldDeleteRemoteClip = preferenceProvider.getBooleanKey(AUTO_DELETE_REMOTE_PREF, false),
        shouldDeletePinnedClip = preferenceProvider.getBooleanKey(AUTO_DELETE_PINNED_PREF, false),
        dayNumber = preferenceProvider.getIntKey(AUTO_DELETE_DAY_PREF, 1),
        excludeTags = preferenceProvider.getStringSet(AUTO_DELETE_EXCLUDE_TAGS_PREF, setOf(ClipTag.LOCK.small()))
    )
    fun setAutoDeleteSetting(setting: AutoDeleteSetting) {
        preferenceProvider.putBooleanKey(AUTO_DELETE_REMOTE_PREF, setting.shouldDeleteRemoteClip)
        preferenceProvider.putBooleanKey(AUTO_DELETE_PINNED_PREF, setting.shouldDeletePinnedClip)
        preferenceProvider.putIntKey(AUTO_DELETE_DAY_PREF, setting.dayNumber)
        preferenceProvider.setStringSet(AUTO_DELETE_EXCLUDE_TAGS_PREF, setting.excludeTags)
        notifyListeners(AUTO_DELETE_DATA_PREF, setting)
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

    data class AutoDeleteSetting(
        val shouldDeleteRemoteClip: Boolean,
        val shouldDeletePinnedClip: Boolean,
        val dayNumber: Int,
        val excludeTags: Set<String>,
    )
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
    DATABASE_DELETE_BINDING,
    SUGGESTION_BUBBLE_X_GRAVITY,
    SUGGESTION_BUBBLE_Y_POS,
    SUGGESTION_BUBBLE_COORDINATES,
    CLIPBOARD_CLEAR_PREF,
    AUTO_DELETE_PREF,
    AUTO_DELETE_DAY_PREF,
    AUTO_DELETE_REMOTE_PREF,
    AUTO_DELETE_PINNED_PREF,
    AUTO_DELETE_EXCLUDE_TAGS_PREF,
    AUTO_DELETE_DATA_PREF,
)
@Retention(AnnotationRetention.SOURCE)
annotation class AppSettingKeys {
    companion object Keys {
        const val IMPROVE_DETECTION = "improve_detection"
        const val POLICY_DISCLOSURE = "policy_disclosure"
        const val IMAGE_MARKDOWN = "render_image_markdown"
        const val CLIPBOARD_SUGGESTIONS = "clipboard_suggestions"
        const val SUGGESTION_BUBBLE_COORDINATES = "suggestion_bubble_coordinates"
        internal const val SUGGESTION_BUBBLE_Y_POS = "suggestion_bubble_y_pos"
        internal const val SUGGESTION_BUBBLE_X_GRAVITY = "suggestion_bubble_x_gravity"
        const val SWIPE_DELETE_CLIP_ITEM = "swipe_delete_clip_item"
        const val CLIP_TEXT_TRIMMING = "clip_text_trimming"
        const val ON_BOARDING_SCREEN = "tutorial_pref"
        const val CLIPBOARD_BLACKLIST_APPS = "blacklist_pref"
        const val SHOW_SEARCH_FEATURE = "to_show_search_feature"
        const val DATABASE_AUTO_SYNC = "autoSync_pref"
        const val DATABASE_BINDING = "bind_pref"
        const val DATABASE_DELETE_BINDING = "bindDelete_pref"
        const val CLIPBOARD_CLEAR_PREF = "clipboard_clear_pref"
        const val AUTO_DELETE_PREF = "auto_delete_pref"
        internal const val AUTO_DELETE_DAY_PREF = "auto_delete_day_pref"
        internal const val AUTO_DELETE_REMOTE_PREF = "auto_delete_remote_pref"
        internal const val AUTO_DELETE_PINNED_PREF = "auto_delete_pinned_pref"
        internal const val AUTO_DELETE_EXCLUDE_TAGS_PREF = "auto_delete_exclude_tags_pref"
        internal const val AUTO_DELETE_DATA_PREF = "auto_delete_data_pref"
    }
}