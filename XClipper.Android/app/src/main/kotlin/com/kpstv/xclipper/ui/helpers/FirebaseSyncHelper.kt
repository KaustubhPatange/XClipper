package com.kpstv.xclipper.ui.helpers

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kpstv.xclipper.App
import com.kpstv.xclipper.BuildConfig
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.extensions.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This instance of Firebase will be used for clipboard synchronization as default one will
 * be used for crash reporting.
 */
object FirebaseSyncHelper {
    private const val APP_NAME = "fb_xclipper_sync"

    private const val MIGRATION_FROM_VERSION = 18
    private const val MIGRATE_EXIST_PREF = "fb_migrate_pref"

    // Migrate existing FirebaseApp to new APP_NAME
    fun migrate(context: Context, preferenceProvider: PreferenceProvider, firebaseProvider: FirebaseProvider) {
        CoroutineScope(Dispatchers.Main).launch {
            if (BuildConfig.VERSION_CODE == MIGRATION_FROM_VERSION && isRegistered(context)
                && !preferenceProvider.getBooleanKey(MIGRATE_EXIST_PREF, false)
            ) {
                firebaseProvider.removeDevice(App.DeviceID)
                FirebaseApp.getApps(context).forEach { app ->
                    Firebase.auth(app).signOut()
                    app.delete()
                }
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.sync_register_title)
                    .setMessage(R.string.sync_register_text)
                    .setPositiveButton(R.string.alright, null)
                    .setNeutralButton(R.string.learn_more) { _, _ ->
                        Utils.commonUrlLaunch(context, context.getString(R.string.app_docs_device_add))
                    }
                    .setCancelable(false)
                    .show()
            }
            preferenceProvider.putBooleanKey(MIGRATE_EXIST_PREF, true)
        }
    }

    fun isRegistered(context: Context) : Boolean {
        return FirebaseApp.getApps(context).any { it.name == APP_NAME }
    }

    fun register(context: Context, options: FirebaseOptions) {
        FirebaseApp.initializeApp(context, options, APP_NAME)
    }

    fun unregister(): Boolean {
        try {
            FirebaseApp.getInstance(APP_NAME).delete()
            return true
        } catch (e: IllegalStateException) {
            // App does not exist
        }
        return false
    }

    fun get() : FirebaseApp? {
        return try {
            FirebaseApp.getInstance(APP_NAME)
        } catch (e: IllegalStateException) {
            null
        }
    }
}