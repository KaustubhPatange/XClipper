package com.kpstv.xclipper.extensions.utils

import android.content.Context
import android.util.Log
import com.kpstv.xclipper.App
import com.kpstv.xclipper.R
import com.kpstv.xclipper.data.provider.FirebaseProvider
import com.kpstv.xclipper.data.provider.PreferenceProvider
import com.kpstv.xclipper.data.repository.MainRepository
import es.dmoral.toasty.Toasty

class FirebaseUtils(
    private val context: Context,
    private val repository: MainRepository,
    private val firebaseProvider: FirebaseProvider,
    private val preferenceProvider: PreferenceProvider
) {
    private val TAG = FirebaseUtils::class.simpleName

    fun observeDatabaseChangeEvents(): Unit =
        with(context) {
            if (!App.observeFirebase) return@with
            firebaseProvider.observeDataChange(
                changed = {
                    if (App.observeFirebase)
                        repository.updateClip(it?.Clips?.last())
                    Log.e(TAG, "User has changed")
                },
                error = {
                    Log.e(TAG, "Error: ${it.message}")
                },
                deviceValidated = { isValidated ->
                    if (!isValidated) {
                        Utils.logoutFromDatabase(preferenceProvider)
                        Toasty.error(
                            this,
                            getString(R.string.err_device_validate),
                            Toasty.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
}