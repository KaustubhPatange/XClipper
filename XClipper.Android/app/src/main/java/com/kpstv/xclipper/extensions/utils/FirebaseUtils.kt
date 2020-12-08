package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.decrypt
import com.kpstv.xclipper.extensions.enumerations.FirebaseState
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import es.dmoral.toasty.Toasty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider,
    private val preferenceProvider: PreferenceProvider,
    private val dbConnectionProvider: DBConnectionProvider,
    private val notificationHelper: NotificationHelper
) {
    private val TAG = FirebaseUtils::class.simpleName

    private var shownToast = false

    fun observeDatabaseChangeEvents(): Unit =
        with(context) {
            if (firebaseProvider.isObservingChanges()) return@with
            if (!App.observeFirebase) return@with

            HVLog.d("Attached")
            firebaseProvider.observeDataChange(
                changed = { clip -> // Unencrypted data
                    if (App.observeFirebase)
                        repository.updateClip(clip?.decrypt())
                },
                removed = { items -> // Unencrypted listOf data
                    items?.forEach { repository.deleteClip(it) }
                },
                removedAll = {
                    notificationHelper.sendNotification(
                        title = getString(R.string.app_name),
                        message = getString(R.string.data_removed_all)
                    )
                },
                error = {
                    HVLog.d()
                    Log.e(TAG, "Error: ${it.message}")
                },
                deviceValidated = { isValidated ->
                    if (!isValidated) {

                        Utils.logoutFromDatabase(
                            context = context,
                            preferenceProvider = preferenceProvider,
                            dbConnectionProvider = dbConnectionProvider
                        )

                        if (!shownToast) {
                            shownToast = true
                            Toasty.error(
                                this,
                                getString(R.string.err_device_validate),
                                Toasty.LENGTH_LONG
                            ).show()
                        }
                    } else
                        shownToast = false
                },
                inconsistentData = {
                    Toasty.error(context, getString(R.string.inconsistent_data), Toasty.LENGTH_LONG).show()
                }
            )
        }

    /**
     * When the boolean is false it will automatically call [FirebaseProvider.removeDataObservation]
     *
     * If you think it will be only called when the device is removed successfully
     * i.e in [FirebaseProvider.removeDevice].
     */
    private val databaseInitializationObserver = Observer<Boolean> {
        if (it)
            observeDatabaseChangeEvents()
    }

    fun observeDatabaseInitialization() {
        HVLog.d()
        if (!firebaseProvider.isInitialized().hasObservers())
            firebaseProvider.isInitialized().observeForever(databaseInitializationObserver)
    }

    fun removeDatabaseInitializationObservation() {
        HVLog.d()
        firebaseProvider.isInitialized().removeObserver(databaseInitializationObserver)
        firebaseProvider.removeDataObservation()
    }

    fun retrieveFirebaseStatus(): FirebaseState {
        return if (firebaseProvider.isInitialized().value == false)
            FirebaseState.NOT_INITIALIZED
        else FirebaseState.UNKNOWN_ERROR
    }
}