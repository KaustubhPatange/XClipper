package com.kpstv.xclipper

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.Logger
import com.kpstv.xclipper.extensions.utils.FirebaseUtils
import com.kpstv.xclipper.extensions.helper.ClipboardLogDetector
import com.kpstv.xclipper.service.worker.AccessibilityWorker
import com.kpstv.xclipper.service.worker.ExtensionWorker
import com.kpstv.xclipper.ui.fragments.settings.GeneralPreference
import com.kpstv.xclipper.ui.helpers.*
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@Suppress("unused")
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

        CoreNotifications.initialize(this)
        UpdaterNotifications.initialize(this)

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

        /** This will load firebase config setting */
        if (dbConnectionProvider.isValidData()) { // implicit loadDataFromPreference();
            firebaseUtils.get().observeDatabaseChangeEvents()
        }

        val options = dbConnectionProvider.optionsProvider()

        AppThemeHelper.loadTheme(this)
        if (options?.uid.isNullOrBlank()) appSettings.setDatabaseBindingEnabled(false)

        if (!ClipboardLogDetector.isDetectionCompatible(applicationContext)) {
            appSettings.setImproveDetectionEnabled(false)
        }

        GeneralPreference.checkImproveSettingsOnStart(applicationContext, appSettings, preferenceProvider)

        /** Initialize firebase data */
        firebaseProvider.initialize(dbConnectionProvider.optionsProvider())

        /** Initialize workers */
        AccessibilityWorker.schedule(this)

        ExtensionWorker.schedule(this)
        ExtensionWorker.scheduleForOnce(this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}