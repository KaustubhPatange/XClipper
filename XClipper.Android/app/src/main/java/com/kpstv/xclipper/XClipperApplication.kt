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
import com.kpstv.xclipper.service.worker.AccessibilityWorker
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@Suppress("unused")
@SuppressLint("HardwareIds")
@HiltAndroidApp
class XClipperApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var preferenceProvider: PreferenceProvider
    @Inject lateinit var firebaseProvider: FirebaseProvider
    @Inject lateinit var dbConnectionProvider: DBConnectionProvider

    override fun onCreate() {
        super.onCreate()
        init()

        notificationHelper.createChannel()
    }

    private val TAG = javaClass.simpleName

    private fun init() {

        /** Setup HVLog */
        HVLog.Config.init(this)
        HVLog.Config.compactClassName = true

        /** Set device ID at startup */
        DeviceID = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        /** Load settings here */
        DICTIONARY_LANGUAGE = preferenceProvider.getStringKey(LANG_PREF, "en")!!

        /** This will load firebase config setting */
        dbConnectionProvider.loadDataFromPreference()

        DARK_THEME = preferenceProvider.getBooleanKey(DARK_PREF, true)
        showSuggestion = preferenceProvider.getBooleanKey(SUGGESTION_PREF, false)
        runAutoSync = preferenceProvider.getBooleanKey(AUTO_SYNC_PREF, false)
        swipeToDelete = preferenceProvider.getBooleanKey(SWIPE_DELETE_PREF, true)
        trimClipText = preferenceProvider.getBooleanKey(TRIM_CLIP_PREF, false)
        blackListedApps = preferenceProvider.getStringSet(App.BLACKLIST_PREF, mutableSetOf())
        bindDelete = preferenceProvider.getBooleanKey(BIND_DELETE_PREF, false)
        bindToFirebase = if (UID.isBlank()) false else preferenceProvider.getBooleanKey(BIND_PREF, false)

        /** Initialize firebase data */
        firebaseProvider.initialize(dbConnectionProvider.optionsProvider())

        /** Initialize accessibility worker */
        AccessibilityWorker.schedule(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}