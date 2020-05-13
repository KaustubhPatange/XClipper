package com.kpstv.xclipper.data.provider

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.getMaxStorage
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.FValueEventListener
import com.kpstv.xclipper.extensions.cloneToEntries

@ExperimentalStdlibApi
class FirebaseProviderImpl : FirebaseProvider {

    private val TAG = javaClass.simpleName

    private val USER_REF = "users"
    private val CLIP_REF = "Clips"

    private lateinit var user: User
    private var validDevice: Boolean = false
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun isLicensed(): Boolean = if (::user.isInitialized) user.IsLicensed else false

    override fun isValidDevice(): Boolean = validDevice

    override fun uploadData(clip: Clip) {
        if (!::user.isInitialized) {
            database.getReference(USER_REF).child(UID)
                .addListenerForSingleValueEvent(FValueEventListener(
                    onDataChange = { snap ->
                        val json = gson.toJson(snap.value)
                        gson.fromJson(json, User::class.java).also { user = it }

                        saveData(clip)
                    },
                    onError = {
                        Log.e(TAG, "Error: Saving data")
                    }
                ))
        } else
            saveData(clip)
    }

    private fun saveData(clip: Clip) {
        val list: ArrayList<Clip> = if (user.Clips == null)
            ArrayList()
        else
            ArrayList(user.Clips?.filter { it.data != clip.data }!!)

        val size = getMaxStorage(isLicensed())

        if (list.size >= size)
            list.removeFirst()
        list.add(clip)

        database.getReference(USER_REF).child(UID).child(CLIP_REF)
            .setValue(list.cloneToEntries()) { error, _ ->
                if (error == null) {
                    user.Clips = list
                    Log.e(TAG, "Firebase Saved: ${clip.data?.Decrypt()}")
                } else
                    Log.e(TAG, "Error: ${error.message}")
            }
    }

    override fun observeDataChange(
        changed: (User?) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit
    ) {
        database.getReference(USER_REF).child(UID)
            .addValueEventListener(FValueEventListener(
                onDataChange = { snap ->
                    val json = gson.toJson(snap.value)
                    gson.fromJson(json, User::class.java).also { user = it }

                    // Check for device validation
                    (user.Devices ?: ArrayList()).forEach { device ->
                        if (device.ID == DeviceID) validDevice = true
                    }
                    deviceValidated.invoke(validDevice)

                    if (json != null)
                        changed.invoke(user)
                    else error.invoke(NullPointerException("Database is null"))
                },
                onError = {
                    error.invoke(it.toException())
                }
            ))
    }

}