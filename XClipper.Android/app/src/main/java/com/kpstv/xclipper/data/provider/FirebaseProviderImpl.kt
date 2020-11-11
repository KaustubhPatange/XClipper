package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kpstv.hvlog.HVLog
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
import com.kpstv.xclipper.extensions.listeners.FChildEventListener
import com.kpstv.xclipper.extensions.listeners.FValueEventListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import javax.inject.Inject

@ExperimentalStdlibApi
class FirebaseProviderImpl @Inject constructor(
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

        HVLog.d()
        Log.e(TAG, "Firebase Database has been initialized")
    }

    override fun isLicensed(): Boolean = user?.IsLicensed ?: false

    override fun isValidDevice(): Boolean = validDevice

    override fun getLicenseStrategy() = licenseStrategy

    override fun clearData() {
        user = null
    }

    override fun addDevice(DeviceId: String, responseListener: ResponseListener<Unit>) {
        HVLog.d("Adding Device...")
        workWithData(ValidationContext.ForceInvoke) {
            HVLog.d("Inside WorkWithData...")
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices ?: listOf())
                else ArrayList<Device>()

            checkForUserDetailsAndUpdateLocal()

            if (list.size >= APP_MAX_DEVICE) {
                responseListener.onError(Exception("Maximum device connection reached"))
                HVLog.w("Maximum device reached...")
                return@workWithData
            }

            /** For some reasons [DeviceID] already exist in the database
             *  we will post success response.
             */
            if (list.count { it.id == DeviceId } > 0) {
                responseListener.onComplete(Unit)
                HVLog.w("DeviceID already present")
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
        HVLog.d("Removing device...")
        workWithData(ValidationContext.ForceInvoke) {
            HVLog.d("Inside WorkWithData...")
            val list =
                if (user?.Devices != null) ArrayList(user?.Devices ?: listOf())
                else ArrayList<Device>()

            if (list.count { it.id == DeviceId } <= 0) {
                responseListener.onError(Exception("No device found with this ID"))
                HVLog.d("No device ID found...")
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
        HVLog.d()

        Log.e(TAG, "ListSize: ${list.size}, List: $list")
        isDeviceAdding = true

        /** Must pass toList to firebase otherwise it add list as linear data. */
        database.getReference(USER_REF).child(UID).child(DEVICE_REF)
            .setValue(list.toList()) { error, _ ->
                HVLog.d("Work completed, ${error?.message}")
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
        HVLog.d()
        workWithData {
            HVLog.d("To process $it")
            if (it)
                replace(unencryptedOldClip, unencryptedNewClip)
        }
    }

    override fun deleteMultipleData(unencryptedClips: List<Clip>) {
        HVLog.d()
        workWithData {
            HVLog.d("To process $it")
            if (it)
                removeData(unencryptedClips)
        }
    }

    override fun deleteData(unencryptedClip: Clip) {
        HVLog.d()
        workWithData {
            HVLog.d("To process $it")
            if (it)
                removeData(unencryptedClip)
        }
    }

    override fun uploadData(unencryptedClip: Clip) {
        HVLog.d()
        workWithData {
            HVLog.d("To process $it")
            if (it)
                saveData(unencryptedClip)
        }
    }

    private fun replace(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        /** Save data when clips are null */
        HVLog.d()
        if (user?.Clips == null) {
            HVLog.d("Saving first data")
            saveData(unencryptedNewClip)
        } else {
            val list =
                ArrayList(user?.Clips?.filter { it.data?.Decrypt() != unencryptedOldClip.data } ?: listOf())

            list.add(unencryptedNewClip.encrypt())

            pushDataToFirebase(list)
        }
    }

    private fun removeData(unencryptedClip: Clip) {
        HVLog.d()
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
        HVLog.d()

        checkForUserDetailsAndUpdateLocal()

        val list: ArrayList<Clip> = if (user?.Clips == null)
            ArrayList()
        else
            ArrayList(user?.Clips?.filter { it.data?.Decrypt() != unencryptedClip.data } ?: listOf())
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
        HVLog.d()
        database.getReference(USER_REF).child(UID).child(CLIP_REF)
            .setValue(list.cloneToEntries()) { error, _ ->
                HVLog.d("Completed, ${error?.message}")
                if (error == null) {
                    user?.Clips = list
                } else
                    Log.e(TAG, "Error: ${error.message}")
            }
    }

    override fun getAllClipData(block: (List<Clip>?) -> Unit) {
        HVLog.d()
        workWithData(ValidationContext.ForceInvoke) {
            HVLog.d("To process $it")
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
    private inline fun workWithData(
        validationContext: ValidationContext = ValidationContext.Default,
        crossinline block: (Boolean) -> Unit
    ) {
        HVLog.d()
        /**
         * Make sure the device is a valid device so that we can make connection
         * and work with the database.
         *
         * This check will make sure that user can only update firebase database
         *  when following criteria satisfies
         */
        if (validationContext == ValidationContext.Default && !bindToFirebase || !dbConnectionProvider.isValidData()) {
            HVLog.d("Returning false - 1")
            block.invoke(false)
            return
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false) {
            val options = dbConnectionProvider.optionsProvider()
            if (options != null)
                initialize(options)
            else {
                HVLog.d("Returning false - 2")
                block.invoke(false)
                return
            }
        }

        if (user == null || validationContext == ValidationContext.ForceInvoke) {
            HVLog.d("Getting user for first time")
            database.getReference(USER_REF).child(UID)
                .addListenerForSingleValueEvent(FValueEventListener(
                    onDataChange = { snap ->
                        val json = gson.toJson(snap.value)
                        gson.fromJson(json, User::class.java).also { user = it }
                        if (!validateUser()) {
                            block.invoke(false)
                            return@FValueEventListener
                        }
                        block.invoke(true)
                    },
                    onError = {
                        Log.e(TAG, "Error: ${it.message}")
                        block.invoke(false)
                    }
                ))
        } else{
            if (!validateUser()) {
                block.invoke(false)
                return
            }
            block.invoke(true)
        }
    }

    override fun isObservingChanges() = ::valueListener.isInitialized && ::childListener.isInitialized

    private lateinit var valueListener: FValueEventListener
    private lateinit var childListener: FChildEventListener
    private var inconsistentDataListener: SimpleFunction? = null
    override fun observeDataChange(
        changed: (Clip?) -> Unit,
        removed: (List<String>?) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit,
        inconsistentData: SimpleFunction
    ) {
        HVLog.d()
        if (!dbConnectionProvider.isValidData()) {
            HVLog.d("Invalid account preference")
            error.invoke(Exception("Invalid account preference"))
            return
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false)
            initialize(dbConnectionProvider.optionsProvider())

        if (UID.isEmpty()) {
            HVLog.d("Empty UID")
            return
        }

        inconsistentDataListener = inconsistentData

        valueListener = FValueEventListener(
            onDataChange = { snap ->
                HVLog.d("OnDataChanging")
                val json = gson.toJson(snap.value)
                val firebaseUser = gson.fromJson(json, User::class.java)

                /** Update the properties */
                checkForUserDetailsAndUpdateLocal()

                /** Check for device validation */
                validDevice = (firebaseUser?.Devices ?: mutableListOf()).count {
                    it.id == DeviceID
                } > 0

                /** Device validation is causing problem, so only invoke it when
                 *  device is not adding.*/
                if (!isDeviceAdding)
                    deviceValidated.invoke(validDevice)

                if (json == null)
                    error.invoke(Exception("Database is null"))

                if (bindDelete) {
                    if (!user?.Clips.isNullOrEmpty()) {
                        val userClips = user?.Clips?.decrypt()?.map { it.data!! }
                        val firebaseClips = firebaseUser?.Clips?.decrypt()?.map { it.data!! }
                        userClips?.minus(firebaseClips!!)
                            ?.let { if (it.isNotEmpty()) mainThread { removed.invoke(it) } }
                    }
                }

                if (!isDeviceAdding && !validDevice)
                    isInitialized.postValue(false)

                user = firebaseUser
            },
            onError = {
                error.invoke(it.toException())
                HVLog.d("onError")
            }
        )

        childListener = FChildEventListener(
            /**
             * Do not try to think & add [FChildEventListener.onDataRemoved] listener hoping
             * that it would solve linear deletion problem. Spoiler alert it doesn't but it
             * makes things even worse.
             *
             * Like if you delete an item in the middle of the list, it will shrink by calling
             * [FChildEventListener.onDataRemoved] every time a child is changed. Better to keep
             * the logic to [FValueEventListener.onDataChange] itself.
             */
            onDataAdded = { snap ->
                if (validDevice) {
                    val json = gson.toJson(snap.value)
                    val clip  = gson.fromJson(json, Clip::class.java)

                    if (clip != null) {
                        changed.invoke(clip)
                    }
                }
            }
        )

        database.getReference(USER_REF).child(UID).child(CLIP_REF)
            .addChildEventListener(childListener)
        database.getReference(USER_REF).child(UID)
            .addValueEventListener(valueListener)
    }

    /**
     * The method will update the local values needed for
     * later purposes.
     */
    private fun checkForUserDetailsAndUpdateLocal() {
        HVLog.d()

        APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())
        user?.LicenseStrategy?.let { licenseStrategy.postValue(it) }
    }

    private fun validateUser(): Boolean {
        for (clip in user?.Clips ?: listOf()) {
            // This will be valid if user suppose manually remove a random node
            // which makes the tree inconsistent.
            if (clip == null ) {
                HVLog.d(m = "Inconsistent data detected")
                mainThread { inconsistentDataListener?.invoke() }
                return false
            }
        }
        return true
    }

    private fun removeUserDetailsAndUpdateLocal() {
        HVLog.d()
        user = null
        licenseStrategy.postValue(LicenseType.Invalid)
        APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())
    }

    override fun removeDataObservation() {
        HVLog.d()
        if (::database.isInitialized && ::valueListener.isInitialized) {
            database.getReference(USER_REF).child(UID)
                .removeEventListener(valueListener)
            database.getReference(USER_REF).child(UID).child(CLIP_REF)
                .removeEventListener(childListener)
            removeUserDetailsAndUpdateLocal()
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