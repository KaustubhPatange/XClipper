package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import com.kpstv.xclipper.extensions.Coroutines
import com.kpstv.xclipper.extensions.enumerations.FirebaseState
import com.kpstv.xclipper.extensions.toInt
import com.kpstv.xclipper.ui.helpers.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
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

    fun observeDatabaseChangeEvents(): Unit = with(context) {
            if (firebaseProvider.isObservingChanges()) return@with
            if (!App.observeFirebase) return@with
            HVLog.d("Attached")
            firebaseProvider.observeDataChange(
                changed = { clips -> // Unencrypted data
                    if (App.observeFirebase) {
                        insertAllClips(clips)
                    }
                },
                removed = { items -> // Unencrypted listOf data
                    Coroutines.io {
                        items?.forEach { repository.deleteClip(it) }
                    }
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

    private fun insertAllClips(clips: List<Clip>) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            when {
                // TODO: Refactor the database & make sure to eliminate notification helper from it.
                clips.size == 1 -> {
                    repository.updateClip(clips[0])
                }
                clips.size > 5 -> {
                    repository.disableNotify()
                    var addedClips = 0
                    clips.forEach { addedClips += repository.updateClip(it).toInt() }
                    if (addedClips > 0) {
                        notificationHelper.pushNotification(
                            text = "$addedClips ${context.getString(R.string.multi_clips_added)}",
                            withActions = false
                        )
                    }
                    repository.enableNotify()
                }
                else -> {
                    clips.forEach { repository.updateClip(it) }
                }
            }
        }
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