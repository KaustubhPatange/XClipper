package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.experimental.Experimental
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kpstv.license.Encryption.Decrypt
import com.kpstv.xclipper.App.APP_MAX_DEVICE
import com.kpstv.xclipper.App.APP_MAX_ITEM
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.bindDelete
import com.kpstv.xclipper.App.bindToFirebase
import com.kpstv.xclipper.App.getMaxConnection
import com.kpstv.xclipper.App.getMaxStorage
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.listeners.FValueEventListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
    private var licenseStrategy = MutableLiveData(LicenseType.Invalid)
    private lateinit var database: FirebaseDatabase

    /**
     * There is drastic problem when device is being added. The thing is lot's of
     * internal calls are made to [FBOptions], so during pushing device data to firebase
     * in [observeDataChange] listener it still returns the cache version of data where the
     * device is not added yet. Due to this device validation fails and causing
     * [DBConnectionProvider] to run [DBConnectionProvider.detachDataFromAll] which is
     * removing all the preference related to connection
     *
     * The only solution I found is to set [isDeviceAdding] to true to know if device is
     * being added or not. In such case device validation will not be invoked.
     */
    private var isDeviceAdding = false

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

        if (FirebaseApp.getApps(context).isEmpty())
            FirebaseApp.initializeApp(context, firebaseOptions)

        database = Firebase.database(options.endpoint)
        isInitialized.postValue(true)

        Log.e(TAG, "Firebase Database has been initialized")
    }

    override fun isLicensed(): Boolean = user?.IsLicensed ?: false

    override fun isValidDevice(): Boolean = validDevice

    override fun getLicenseStrategy() = licenseStrategy

    override fun clearData() {
        user = null
    }

    override fun addDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        workWithData(ValidationContext.ForceInvoke) {
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices!!)
                else ArrayList<Device>()

            checkForUserDetailsAndUpdateLocal()

            if (list.size >= APP_MAX_DEVICE) {
                responseListener.onError(Exception("Maximum device connection reached"))
                return@workWithData
            }

            /** For some reasons [DeviceID] already exist in the database
             *  we will post success response.
             */
            if (list.count { it.id == DeviceId } > 0) {
                responseListener.onComplete(Unit)
                return@workWithData
            }

            list.add(Device(DeviceId, Build.VERSION.SDK_INT, Build.MODEL))

            updateDeviceList(
                list = list,
                uploadStatus = UploadStatus.Adding,
                responseListener = responseListener
            )
        }
    }


    override fun removeDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        workWithData(ValidationContext.ForceInvoke) {
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices!!)
                else ArrayList<Device>()

            if (list.count { it.id == DeviceId } <= 0) {
                responseListener.onError(Exception("No device found with this ID"))
                return@workWithData
            }

            val filterList = list.filter { it.id != DeviceId }

            updateDeviceList(
                list = filterList,
                uploadStatus = UploadStatus.Removing,
                responseListener = responseListener
            )
        }
    }

    /**
     * This will update the device list with the given list.
     *
     * When the [uploadStatus] is [UploadStatus.Removing] it will remove
     * the firebase data change observation through [removeDataObservation]
     * and make [isInitialized] to true.
     */
    private fun updateDeviceList(
        list: List<Device>,
        uploadStatus: UploadStatus,
        responseListener: ResponseListener<Unit>
    ) {

        Log.e(TAG, "ListSize: ${list.size}, List: $list")
        isDeviceAdding = true

        /** Must pass toList to firebase otherwise it add list as linear data. */
        database.getReference(USER_REF).child(UID).child(DEVICE_REF)
            .setValue(list.toList()) { error, _ ->
                if (error == null) {
                    responseListener.onComplete(Unit)
                    isDeviceAdding = false
                } else
                    responseListener.onError(Exception(error.message))

                if (uploadStatus == UploadStatus.Removing) {
                    isInitialized.postValue(false)
                    removeDataObservation()
                }
            }
    }

    override fun replaceData(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        workWithData {
            if (it)
                replace(unencryptedOldClip, unencryptedNewClip)
        }
    }

    override fun deleteMultipleData(unencryptedClips: List<Clip>) {
        workWithData {
            if (it)
                removeData(unencryptedClips)
        }
    }

    override fun deleteData(unencryptedClip: Clip) {
        workWithData {
            if (it)
                removeData(unencryptedClip)
        }
    }

    override fun uploadData(unencryptedClip: Clip) {
        workWithData {
            if (it)
                saveData(unencryptedClip)
        }
    }

    private fun replace(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        /** Save data when clips are null */
        if (user?.Clips == null) {
            saveData(unencryptedNewClip)
        } else {
            val list =
                ArrayList(user?.Clips!!.filter { it.data?.Decrypt() != unencryptedOldClip.data })

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

        checkForUserDetailsAndUpdateLocal()

        val list: ArrayList<Clip> = if (user?.Clips == null)
            ArrayList()
        else
            ArrayList(user?.Clips?.filter { it.data?.Decrypt() != unencryptedClip.data }!!)
        val size = APP_MAX_ITEM

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
                } else
                    Log.e(TAG, "Error: ${error.message}")
            }
    }

    override fun getAllClipData(block: (List<Clip>?) -> Unit) {
        workWithData(ValidationContext.ForceInvoke) {
            if (it)
                block.invoke(user?.Clips?.decrypt())
            else
                block.invoke(null)
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
        block: (Boolean) -> Unit
    ) {

        /**
         * Make sure the device is a valid device so that we can make connection
         * and work with the database.
         *
         * This check will make sure that user can only update firebase database
         *  when following criteria satisfies
         */
        if (validationContext == ValidationContext.Default && !bindToFirebase && !dbConnectionProvider.isValidData()) {
            block.invoke(false)
            return
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false) {
            val options = dbConnectionProvider.optionsProvider()
            if (options != null)
                initialize(options)
            else {
                block.invoke(false)
                return
            }
        }

        if (user == null || validationContext == ValidationContext.ForceInvoke) {

            database.getReference(USER_REF).child(UID)
                .addListenerForSingleValueEvent(FValueEventListener(
                    onDataChange = { snap ->
                        val json = gson.toJson(snap.value)
                        gson.fromJson(json, User::class.java).also { user = it }
                        block.invoke(true)
                    },
                    onError = {
                        Log.e(TAG, "Error: ${it.message}")
                        block.invoke(false)
                    }
                ))
        } else
            block.invoke(true)
    }

    private lateinit var valueListener: FValueEventListener
    override fun observeDataChange(
        changed: (User?) -> Unit,
        removed: (List<String>) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit
    ) {
        if (!dbConnectionProvider.isValidData()) {
            error.invoke(Exception("Invalid account preference"))
            return
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false)
            initialize(dbConnectionProvider.optionsProvider())

        if (UID.isEmpty()) return

        valueListener = FValueEventListener(
            onDataChange = { snap ->
                val json = gson.toJson(snap.value)
                val firebaseUser = gson.fromJson(json, User::class.java)

                /** Check for device validation */
                validDevice = (firebaseUser?.Devices ?: mutableListOf()).count {
                    it.id == DeviceID
                } > 0

                /** Update the properties */
                checkForUserDetailsAndUpdateLocal()

                /** Device validation is causing problem, so only invoke it when
                 *  device is not adding.*/
                if (!isDeviceAdding)
                    deviceValidated.invoke(validDevice)

                /** Check for deletes, doing it on IO thread so rest job will be
                 *  in continuation like normal. Publishing data will be posted on
                 *  main thread (Reason: to process large number of list up to 1000). */
                ioThread {
                    if (bindDelete) {
                        if (!user?.Clips.isNullOrEmpty()) {
                            val userClips = user?.Clips?.decrypt()?.map { it.data!! }
                            val firebaseClips = firebaseUser?.Clips?.decrypt()?.map { it.data!! }
                            userClips?.minus(firebaseClips!!)
                                ?.let { if (it.isNotEmpty()) mainThread { removed.invoke(it) } }
                        }
                    }
                }

                if (json != null && validDevice)
                    changed.invoke(firebaseUser)
                else
                    error.invoke(Exception("Database is null"))

                if (!isDeviceAdding && !validDevice)
                    isInitialized.postValue(false)

                user = firebaseUser
            },
            onError = {
                error.invoke(it.toException())
            }
        )

        database.getReference(USER_REF).child(UID)
            .addValueEventListener(valueListener)
    }

    /**
     * The method will update the local values needed for
     * later purposes.
     */
    private fun checkForUserDetailsAndUpdateLocal() {
        APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())
        user?.LicenseStrategy?.let { licenseStrategy.postValue(it) }
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

    private enum class UploadStatus {
        Adding,
        Removing
    }
}