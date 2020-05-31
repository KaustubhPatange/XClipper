package com.kpstv.xclipper.data.provider

import android.os.Build
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.kpstv.xclipper.App.BindToFirebase
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.getMaxStorage
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.cloneToEntries
import com.kpstv.xclipper.extensions.listeners.FValueEventListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener

@ExperimentalStdlibApi
class FirebaseProviderImpl : FirebaseProvider {

    private val TAG = javaClass.simpleName

    private val USER_REF = "users"
    private val CLIP_REF = "Clips"
    private val DEVICE_REF = "Devices"

    private var user: User? = null
    private var validDevice: Boolean = false
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    override fun isLicensed(): Boolean = user?.IsLicensed ?: false

    override fun isValidDevice(): Boolean = validDevice

    override fun clearData() {
        user = null
    }

    override fun addDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        workWithData(ValidationContext.ForceInvoke) {
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices!!)
                else ArrayList<Device>()

            if (list.count { it.ID == DeviceId } > 0) return@workWithData

            list.add(Device(DeviceId, Build.VERSION.SDK_INT, Build.MODEL))

            updateDeviceList(list, responseListener)
        }
    }


    override fun removeDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        workWithData(ValidationContext.ForceInvoke) {
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices!!)
                else ArrayList<Device>()

            if (list.count { it.ID == DeviceId } > 0) return@workWithData

            val filterList = list.filter { it.ID == DeviceId }

            updateDeviceList(filterList, responseListener)
        }
    }

    private fun updateDeviceList(list: List<Device>, responseListener: ResponseListener<Unit>) {

        Log.e(TAG, "ListSize: ${list.size}, List: $list")

        /** Must pass toList to firebase otherwise it add list as linear data. */
        database.getReference(USER_REF).child(UID).child(DEVICE_REF)
            .setValue(list.toList()) { error, _ ->
                if (error == null) {
                    responseListener.onComplete(Unit)
                }
                else
                    responseListener.onError(java.lang.Exception(error.message))
            }
    }

    override fun replaceData(oldClip: Clip, newClip: Clip) {
        workWithData {
            replace(oldClip, newClip)
        }
    }

    override fun deleteMultipleData(clips: List<Clip>) {
        workWithData {
            removeData(clips)
        }
    }

    override fun deleteData(clip: Clip) {
        workWithData {
            removeData(clip)
        }
    }

    override fun uploadData(clip: Clip) {
        workWithData {
            saveData(clip)
        }
    }

    private fun replace(oldClip: Clip, newClip: Clip) {
        /** Save data when clips are null */
        if (user?.Clips == null) {
            saveData(newClip)
        } else {
            val list = ArrayList(user?.Clips!!.filter { it.data != oldClip.data })

            list.add(newClip)

            updateData(list)
        }
    }

    private fun removeData(clip: Clip) {
        removeData(List(1) { clip })
    }

    private fun removeData(clips: List<Clip>) {
        if (user?.Clips == null) return

        val list = ArrayList(user?.Clips!!)

        val dataList = clips.map { it.data }

        list.removeAll {
            it.data in dataList
        }

        /*
        user.Clips!!.forEach { clip1 ->
            clips.forEach { clip2 ->
                if (clip1.data != clip2.data)
            }
        }*/

        //  list.removeAll(clips)

        updateData(list)
    }

    private fun saveData(clip: Clip) {
        val list: ArrayList<Clip> = if (user?.Clips == null)
            ArrayList()
        else
            ArrayList(user?.Clips?.filter { it.data != clip.data }!!)
        val size = getMaxStorage(isLicensed())

        if (list.size >= size)
            list.removeFirst()
        list.add(clip)
        updateData(list)
    }

    /**
     * A common provider which will submit the data to firebase.
     */
    private fun updateData(list: ArrayList<Clip>) {
        database.getReference(USER_REF).child(UID).child(CLIP_REF)
            .setValue(list.cloneToEntries()) { error, _ ->
                if (error == null) {
                    user?.Clips = list
                    Log.e(TAG, "Firebase: Success")
                } else
                    Log.e(TAG, "Error: ${error.message}")
            }
    }

    override fun getAllClipData(block: (List<Clip>?) -> Unit) {
        workWithData(ValidationContext.ForceInvoke) {
            block.invoke(user?.Clips)
        }
    }

    /**
     * A common provider to execute some functions straightaway on
     * firebase database.
     *
     * @param validationContext Specify the context for invoking methods
     */
    private fun workWithData(
        validationContext: ValidationContext = ValidationContext.Default,
        block: () -> Unit
    ) {

        /** This check will make sure that user can only update firebase database
         *  when following criteria satisfies
         */
        if (validationContext == ValidationContext.Default && !BindToFirebase) return

        if (user == null) {
            database.getReference(USER_REF).child(UID)
                .addListenerForSingleValueEvent(FValueEventListener(
                    onDataChange = { snap ->
                        Log.e(TAG, "Check 4")
                        val json = gson.toJson(snap.value)
                        gson.fromJson(json, User::class.java).also { user = it }
                        block.invoke()
                    },
                    onError = {
                        Log.e(TAG, "Error: ${it.message}")
                    }
                ))
        } else
            block.invoke()

        Log.e(TAG, "Check 3")
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
                    (user?.Devices ?: ArrayList()).forEach { device ->
                        if (device.ID == DeviceID) {
                            validDevice = true
                            return@forEach
                        }
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

    private enum class ValidationContext {
        Default,
        ForceInvoke
    }
}