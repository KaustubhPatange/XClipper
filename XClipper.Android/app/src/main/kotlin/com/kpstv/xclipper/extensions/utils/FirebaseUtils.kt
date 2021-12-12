package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import com.kpstv.hvlog.HVLog
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.helper.ClipRepositoryHelper
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.provider.DBConnectionProvider
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.extensions.enumerations.FirebaseState
import com.kpstv.xclipper.ui.helpers.AppSettings
import com.kpstv.xclipper.ui.helpers.Notifications
import com.kpstv.xclipper.ui.helpers.connection.ConnectionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUtils @Inject constructor(
  @ApplicationContext private val context: Context,
  private val clipRepositoryHelper: ClipRepositoryHelper,
  private val firebaseProvider: FirebaseProvider,
  private val appSettings: AppSettings,
  private val dbConnectionProvider: DBConnectionProvider,
) {
    private val TAG = FirebaseUtils::class.simpleName

    private var shownToast = false

    fun observeDatabaseChangeEvents(): Unit = with(context) {
            if (firebaseProvider.isObservingChanges()) return@with
            HVLog.d("Attached")
            firebaseProvider.observeDataChange(
                changed = { clips -> // Unencrypted data
                    insertAllClips(clips)
                },
                removed = { items -> // Unencrypted listOf data
                    clipRepositoryHelper.deleteClip(items)
                },
                removedAll = {

                    Notifications.sendNotification(
                        context = context,
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

                        ConnectionHelper.logoutFromDatabase(
                            context = context,
                            appSettings = appSettings,
                            dbConnectionProvider = dbConnectionProvider
                        )

                        if (!shownToast) {
                            shownToast = true
                            Toasty.error(this, getString(R.string.err_device_validate), Toasty.LENGTH_LONG).show()
                        }
                    } else {
                        shownToast = false
                    }
                },
                inconsistentData = {
                    Toasty.error(context, getString(R.string.inconsistent_data), Toasty.LENGTH_LONG).show()
                }
            )
        }

    private fun insertAllClips(clips: List<Clip>) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            when (clips.size) {
                1 -> {
                    clipRepositoryHelper.insertOrUpdateClip(
                        clip = clips[0],
                        toFirebase = false
                    )
                }
                else -> {
                    clipRepositoryHelper.insertOrUpdateClip(
                        clips = clips,
                        toFirebase = false
                    )
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