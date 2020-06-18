package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kpstv.license.Decrypt
import com.kpstv.xclipper.App.BindToFirebase
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.getMaxConnection
import com.kpstv.xclipper.App.getMaxStorage
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.cloneToEntries
import com.kpstv.xclipper.extensions.encrypt
import com.kpstv.xclipper.extensions.listeners.FValueEventListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener

@ExperimentalStdlibApi
class FirebaseProviderImpl(
    private val context: Context,
    private val dbConnectionProvider: DBConnectionProvider
) : FirebaseProvider {

    private val TAG = javaClass.simpleName

    private val USER_REF = "users"
    private val CLIP_REF = "Clips"
    private val DEVICE_REF = "Devices"

    private var isInitialized = MutableLiveData(false)
    private var user: User? = null
    private var validDevice: Boolean = false
    private lateinit var database: FirebaseDatabase

    override fun isInitialized() = isInitialized

    override fun initialize(options: FBOptions?) {
        if (options == null) {
            isInitialized.postValue(false)
            return
        }
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(options.apiKey)
            .setApplicationId(options.appId)
            .setDatabaseUrl(options.endpoint)
            .build()

        FirebaseApp.getInstance().delete()

        if (FirebaseApp.getApps(context).isEmpty())
            FirebaseApp.initializeApp(context, firebaseOptions)

        database = Firebase.database(options.endpoint)
        isInitialized.postValue(true)

        Log.e(TAG, "Firebase Database has been initialized")
    }

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

            if (list.size >= getMaxConnection(isLicensed())) {
                responseListener.onError(java.lang.Exception("Maximum device connection reached"))
                return@workWithData
            }

            if (list.count { it.id == DeviceId } > 0) {
                responseListener.onError(java.lang.Exception("Device already exist"))
                return@workWithData
            }

            list.add(Device(DeviceId, Build.VERSION.SDK_INT, Build.MODEL))

            updateDeviceList(list, responseListener)
        }
    }


    override fun removeDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        workWithData(ValidationContext.ForceInvoke) {
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices!!)
                else ArrayList<Device>()

            if (list.count { it.id == DeviceId } <= 0) {
                responseListener.onError(java.lang.Exception("No device found with this ID"))
                return@workWithData
            }

            val filterList = list.filter { it.id != DeviceId }

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
                } else
                    responseListener.onError(java.lang.Exception(error.message))
            }
    }

    override fun replaceData(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        workWithData {
            replace(unencryptedOldClip, unencryptedNewClip)
        }
    }

    override fun deleteMultipleData(unencryptedClips: List<Clip>) {
        workWithData {
            removeData(unencryptedClips)
        }
    }

    override fun deleteData(unencryptedClip: Clip) {
        workWithData {
            removeData(unencryptedClip)
        }
    }

    override fun uploadData(unencryptedClip: Clip) {
        workWithData {
            saveData(unencryptedClip)
        }
    }

    private fun replace(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        /** Save data when clips are null */
        if (user?.Clips == null) {
            saveData(unencryptedNewClip)
        } else {
            val list = ArrayList(user?.Clips!!.filter { it.data?.Decrypt() != unencryptedOldClip.data })

            list.add(unencryptedNewClip.encrypt())

            pushDataToFirebase(list)
        }
    }

    private fun removeData(unencryptedClip: Clip) {
        removeData(List(1) { unencryptedClip })
    }

    private fun removeData(unencryptedClips: List<Clip>) {
        if (user?.Clips == null) return

        val list = ArrayList(user?.Clips!!)

        val dataList = unencryptedClips.map { it.data }

        list.removeAll {
            it.data?.Decrypt() in dataList
        }

        pushDataToFirebase(list)
    }

    private fun saveData(unencryptedClip: Clip) {
        val list: ArrayList<Clip> = if (user?.Clips == null)
            ArrayList()
        else
            ArrayList(user?.Clips?.filter { it.data?.Decrypt() != unencryptedClip.data }!!)
        val size = getMaxStorage(isLicensed())

        if (list.size >= size)
            list.removeFirst()
        list.add(unencryptedClip.encrypt())
        pushDataToFirebase(list)
    }

    /**
     * A common method which will submit the data to firebase.
     */
    private fun pushDataToFirebase(list: ArrayList<Clip>) {
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

        /**
         * Make sure the device is a valid device so that we can make connection
         * and work with the database.
         *
         * This check will make sure that user can only update firebase database
         *  when following criteria satisfies
         */
        if (validationContext == ValidationContext.Default && !BindToFirebase && !dbConnectionProvider.isValidData()) return

        /** Automated initialization of firebase database */
        if (isInitialized.value == false)
            initialize(dbConnectionProvider.optionsProvider())

        if (user == null || validationContext == ValidationContext.ForceInvoke) {

            if (UID.isEmpty()) return

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

    private lateinit var valueListener: FValueEventListener
    override fun observeDataChange(
        changed: (User?) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit
    ) {
        if (!dbConnectionProvider.isValidData()) {
            error.invoke(java.lang.Exception("Invalid account preference"))
            return
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false)
            initialize(dbConnectionProvider.optionsProvider())

        if (UID.isEmpty()) return

        valueListener = FValueEventListener(
            onDataChange = { snap ->
                val json = gson.toJson(snap.value)
                user = gson.fromJson(json, User::class.java)

                // Check for device validation
                validDevice = (user?.Devices ?: mutableListOf()).count {
                    it.id == DeviceID
                } > 0

                // Device validation is causing problem.
                deviceValidated.invoke(validDevice)

                if (json != null && validDevice)
                    changed.invoke(user)
                else
                    error.invoke(Exception("Database is null"))

                if (!validDevice) isInitialized.postValue(false)
            },
            onError = {
                error.invoke(it.toException())
            }
        )


        database.getReference(USER_REF).child(UID)
            .addValueEventListener(valueListener)
    }

    override fun removeDataObservation() {
        if (::database.isInitialized && ::valueListener.isInitialized) {
            database.getReference(USER_REF).child(UID)
                .removeEventListener(valueListener)
        }
    }

    private enum class ValidationContext {
        Default,
        ForceInvoke
    }
}