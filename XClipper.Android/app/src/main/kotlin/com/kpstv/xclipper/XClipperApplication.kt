package com.kpstv.xclipper

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App.AUTO_SYNC_PREF
import com.kpstv.xclipper.App.BIND_DELETE_PREF
import com.kpstv.xclipper.App.BIND_PREF
import com.kpstv.xclipper.App.DARK_PREF
import com.kpstv.xclipper.App.DARK_THEME
import com.kpstv.xclipper.App.DICTIONARY_LANGUAGE
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.LANG_PREF
import com.kpstv.xclipper.App.SUGGESTION_PREF
import com.kpstv.xclipper.App.SWIPE_DELETE_PREF
import com.kpstv.xclipper.App.TRIM_CLIP_PREF
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.bindDelete
import com.kpstv.xclipper.App.bindToFirebase
import com.kpstv.xclipper.App.blackListedApps
import com.kpstv.xclipper.App.runAutoSync
import com.kpstv.xclipper.App.showSuggestion
import com.kpstv.xclipper.App.swipeToDelete
import com.kpstv.xclipper.App.trimClipText
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.Logger
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.service.helper.ClipboardLogDetector
import com.kpstv.xclipper.service.worker.AccessibilityWorker
import com.kpstv.xclipper.service.worker.ExtensionWorker
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.CrashHelper
import com.kpstv.xclipper.ui.helpers.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@Suppress("unused")
@SuppressLint("HardwareIds")
@HiltAndroidApp
class XClipperApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var preferenceProvider: PreferenceProvider
    @Inject lateinit var firebaseProvider: FirebaseProvider
    @Inject lateinit var dbConnectionProvider: DBConnectionProvider
    @Inject lateinit var appSettings: AppSettings
    @Inject lateinit var firebaseUtils: dagger.Lazy<FirebaseUtils>

    override fun onCreate() {
        super.onCreate()
        CrashHelper.inject(this)

        init()

        Notifications.initialize(this)

        Logger.init(BuildConfig.DEBUG)

        if (BuildConfig.DEBUG) {
            Logger.disable(this)
        }
    }

    private val TAG = javaClass.simpleName

    private fun init() {
        /** Setup HVLog */
        HVLog.Config.init(this, reportWhenApplicationCrashed = false)
        HVLog.Config.writeToFile = false
        HVLog.Config.compactClassName = true

        /** Set device ID at startup */
        DeviceID = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        /** Load settings here */
        DICTIONARY_LANGUAGE = preferenceProvider.getStringKey(LANG_PREF, "en")!!

        /** This will load firebase config setting */
        if (dbConnectionProvider.isValidData()) { // implicit loadDataFromPreference();
            firebaseUtils.get().observeDatabaseChangeEvents()
        }

        DARK_THEME = preferenceProvider.getBooleanKey(DARK_PREF, true)
        showSuggestion = preferenceProvider.getBooleanKey(SUGGESTION_PREF, false)
        runAutoSync = preferenceProvider.getBooleanKey(AUTO_SYNC_PREF, false)
        swipeToDelete = preferenceProvider.getBooleanKey(SWIPE_DELETE_PREF, true)
        trimClipText = preferenceProvider.getBooleanKey(TRIM_CLIP_PREF, false)
        blackListedApps = preferenceProvider.getStringSet(App.BLACKLIST_PREF, mutableSetOf())
        bindDelete = preferenceProvider.getBooleanKey(BIND_DELETE_PREF, false)
        bindToFirebase = if (UID.isBlank()) false else preferenceProvider.getBooleanKey(BIND_PREF, false)

        if (!ClipboardLogDetector.isDetectionCompatible(applicationContext)) {
            appSettings.setImproveDetectionEnabled(false)
        }

        GeneralPreference.checkImproveSettingsOnStart(applicationContext, appSettings, preferenceProvider)

        /** Initialize firebase data */
        firebaseProvider.initialize(dbConnectionProvider.optionsProvider())

        /** Initialize workers */
        AccessibilityWorker.schedule(this)
        ExtensionWorker.schedule(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}