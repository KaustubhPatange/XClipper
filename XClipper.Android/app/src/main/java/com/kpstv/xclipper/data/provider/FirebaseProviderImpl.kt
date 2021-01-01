package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kpstv.firebase.DataResponse
import com.kpstv.firebase.ValueEventResponse
import com.kpstv.firebase.extensions.setValueAsync
import com.kpstv.firebase.extensions.singleValueEvent
import com.kpstv.firebase.extensions.valueEventFlow
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
import com.kpstv.xclipper.data.localized.dao.UserEntityDao
import com.kpstv.xclipper.data.model.*
import com.kpstv.xclipper.extensions.*
import com.kpstv.xclipper.extensions.listeners.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class FirebaseProviderImpl @Inject constructor(
    private val context: Context,
    private val dbConnectionProvider: DBConnectionProvider,
    private val currentUserRepository: UserEntityDao
) : FirebaseProvider {

    companion object {
        private const val USER_REF = "users"
        private const val CLIP_REF = "Clips"
        private const val DEVICE_REF = "Devices"
    }

    private val TAG = javaClass.simpleName

    private var isInitialized = MutableLiveData(false)

    private var validDevice: Boolean = false
    private var licenseStrategy = MutableLiveData(LicenseType.Invalid)
    private lateinit var database: FirebaseDatabase

    override fun isInitialized() = isInitialized

    override fun initialize(options: FBOptions?, notifyInitialization: Boolean) {
        if (options == null) {
            isInitialized.postValue(false)
            return
        }
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(options.apiKey)
            .setApplicationId(options.appId)
            .setDatabaseUrl(options.endpoint)
            .build()

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, firebaseOptions)
        }

        database = Firebase.database(options.endpoint)
        if (notifyInitialization)
            isInitialized.postValue(true)

        HVLog.d()
        Log.e(TAG, "Firebase Database has been initialized")
    }

    override fun uninitialized() {
        if (isInitialized.value == true) {
            FirebaseApp.getInstance(FirebaseApp.DEFAULT_APP_NAME).delete()
            isInitialized.postValue(false)
        }
    }

    override suspend fun isLicensed(): Boolean = currentUserRepository.isLicensed() ?: false

    override fun isValidDevice(): Boolean = validDevice

    override fun getLicenseStrategy() = licenseStrategy

    override suspend fun clearData() {
        currentUserRepository.remove()
    }

    override suspend fun addDevice(DeviceId: String): ResponseResult<Unit> {
        HVLog.d("Adding Device...")
        workWithData(ValidationContext.ForceInvoke)
        HVLog.d("Inside WorkWithData...")
        val user = currentUserRepository.get()
        val list =
            if (user?.Devices != null) ArrayList(user.Devices ?: listOf())
            else ArrayList<Device>()

        checkForUserDetailsAndUpdateLocal()

        /** For some reasons [DeviceID] already exist in the database
         *  we will post success response.
         */
        if (list.any { it.id == DeviceId }) {
            HVLog.w("DeviceID already present")
            return ResponseResult.complete(Unit)
        }

        if (list.size >= APP_MAX_DEVICE) {
            HVLog.w("Maximum device reached...")
            return ResponseResult.error(Exception("Maximum device connection reached"))
        }

        list.add(Device(DeviceId, Build.VERSION.SDK_INT, Build.MODEL))

        return updateDeviceList(
            list = list,
            uploadStatus = UploadStatus.Adding
        )
    }


    override suspend fun removeDevice(DeviceId: String): ResponseResult<Unit> {
        HVLog.d("Removing device...")

        workWithData(ValidationContext.ForceInvoke)

        HVLog.d("Inside WorkWithData...")
        val user = currentUserRepository.get()
        val list =
            if (user?.Devices != null) ArrayList(user.Devices ?: listOf())
            else ArrayList<Device>()

        if (list.count { it.id == DeviceId } <= 0) {
            HVLog.d("No device ID found...")
            return ResponseResult.error(Exception("No device found with this ID"))
        }

        val filterList = list.filter { it.id != DeviceId }

        return updateDeviceList(
            list = filterList,
            uploadStatus = UploadStatus.Removing
        )
    }

    /**
     * This will update the device list with the given list.
     *
     * When the [uploadStatus] is [UploadStatus.Removing] it will remove
     * the firebase data change observation through [removeDataObservation]
     * and make [isInitialized] to true.
     */
    private suspend fun updateDeviceList(
        list: List<Device>,
        uploadStatus: UploadStatus
    ): ResponseResult<Unit> {
        HVLog.d()

        if (UID.isBlank()) return ResponseResult.error("Error: Invalid UID")

        Log.e(TAG, "ListSize: ${list.size}, List: $list")

        /** Must pass toList to firebase otherwise it add list as linear data. */
        val result: DataResponse<DatabaseReference> = database.getReference(USER_REF).child(UID).child(DEVICE_REF).setValueAsync(list.toList())

        currentUserRepository.updateDevices(list)

        when(uploadStatus) {
            UploadStatus.Adding -> {
                isInitialized.postValue(true)
            }
            UploadStatus.Removing -> {
                uninitialized()
                removeDataObservation()
            }
        }

        return when(result) {
            is DataResponse.Complete -> ResponseResult.complete(Unit)
            is DataResponse.Error -> ResponseResult.error(result.error)
        }
    }

    override suspend fun replaceData(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        HVLog.d()
        val shouldInvoke = workWithData()
        HVLog.d("To process $shouldInvoke")
        if (shouldInvoke)
            replace(unencryptedOldClip, unencryptedNewClip)
    }

    override suspend fun deleteMultipleData(unencryptedClips: List<Clip>) {
        HVLog.d()
        val shouldInvoke = workWithData()
        HVLog.d("To process $shouldInvoke")
        if (shouldInvoke)
            removeData(unencryptedClips)
    }

    override suspend fun deleteData(unencryptedClip: Clip) {
        HVLog.d()
        val shouldInvoke = workWithData()
        HVLog.d("To process $shouldInvoke")
        if (shouldInvoke)
            removeData(unencryptedClip)
    }

    override suspend fun uploadData(unencryptedClip: Clip) {
        HVLog.d()
        val shouldInvoke = workWithData()
        HVLog.d("To process $shouldInvoke")
        if (shouldInvoke)
            saveData(unencryptedClip)
    }

    private suspend fun replace(unencryptedOldClip: Clip, unencryptedNewClip: Clip) {
        /** Save data when clips are null */
        HVLog.d()
        val user = currentUserRepository.get()
        if (user?.Clips == null) {
            HVLog.d("Saving first data")
            saveData(unencryptedNewClip)
        } else {
            val list =
                ArrayList(user.Clips?.filter { it.data.Decrypt() != unencryptedOldClip.data }
                    ?: listOf())

            list.add(unencryptedNewClip.encrypt())

            pushDataToFirebase(list)
        }
    }

    private suspend fun removeData(unencryptedClip: Clip) {
        HVLog.d()
        removeData(List(1) { unencryptedClip })
    }

    private suspend fun removeData(unencryptedClips: List<Clip>) {
        val user = currentUserRepository.get()
        if (user?.Clips == null) return

        val list = ArrayList(user.Clips!!)

        val dataList = unencryptedClips.map { it.data }

        list.removeAll {
            it.data.Decrypt() in dataList
        }

        pushDataToFirebase(list)
    }

    private suspend fun saveData(unencryptedClip: Clip) {
        HVLog.d()
        checkForUserDetailsAndUpdateLocal()

        val user = currentUserRepository.get()
        val list: ArrayList<Clip> = if (user?.Clips == null)
            ArrayList()
        else
            ArrayList(user.Clips?.filter { it.data.Decrypt() != unencryptedClip.data } ?: listOf())
        val size = APP_MAX_ITEM

        if (list.size >= size)
            list.removeFirst()
        list.add(unencryptedClip.encrypt())
        pushDataToFirebase(list)
    }

    /**
     * A common method which will submit the data to firebase.
     */
    private suspend fun pushDataToFirebase(list: ArrayList<Clip>) {
        HVLog.d()
        val result = database.getReference(USER_REF).child(UID).child(CLIP_REF).setValueAsync(list.cloneToEntries())
        when(result) {
            is DataResponse.Complete -> {
                currentUserRepository.updateClips(list)
            }
            is DataResponse.Error -> {
                Log.e(TAG, "Error: ${result.error.message}")
            }
        }
    }

    override suspend fun getAllClipData(): List<Clip>? {
        HVLog.d()
        val shouldInvoke =  workWithData(ValidationContext.ForceInvoke)
        val user = currentUserRepository.get()
        HVLog.d("To process $shouldInvoke")
        return if (shouldInvoke)
            user?.Clips?.decrypt()
        else null
    }

    /**
     * A common provider to execute some functions straightaway on
     * firebase database.
     *
     * @param validationContext Specify the context for invoking methods
     */
    private suspend inline fun workWithData(
        validationContext: ValidationContext = ValidationContext.Default,
    ): Boolean {
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
            return false
        }

        /** Automated initialization of firebase database */
        if (isInitialized.value == false) {
            val options = dbConnectionProvider.optionsProvider()
            if (options != null)
                initialize(options, false)
            else {
                HVLog.d("Returning false - 2")
                return false
            }
        }

        if (!currentUserRepository.isExist() || validationContext == ValidationContext.ForceInvoke) {
            HVLog.d("Getting user for first time or a force invoke?")
            val result = database.getReference(USER_REF).child(UID).singleValueEvent()
            return when(result) {
                is DataResponse.Complete -> {
                    val json = gson.toJson(result.data.value)
                    val userEntity = UserEntity.from(User.from(json))
                    currentUserRepository.update(userEntity)
                    validateUser()
                }
                is DataResponse.Error -> {
                    Log.e(TAG, "Error: ${result.error.message}")
                    false
                }
            }
        } else {
            if (!validateUser()) {
                return false
            }
            return true
        }
    }

    override fun isObservingChanges() = job?.isActive ?: false

    private var inconsistentDataListener: SimpleFunction? = null
    private var job: CompletableJob? = null
    override fun observeDataChange(
        changed: (List<Clip>) -> Unit,
        removed: (List<String>?) -> Unit,
        removedAll: SimpleFunction,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit,
        inconsistentData: SimpleFunction
    ) {
        job?.cancel()
        job = SupervisorJob()
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

        CoroutineScope(Dispatchers.Main + job!!).launch {
            database.getReference(USER_REF).child(UID).valueEventFlow().collect { result ->
                job?.ensureActive()
                when(result) {
                    is ValueEventResponse.Changed -> {
                        HVLog.d("OnDataChanging")
                        val json = gson.toJson(result.snapshot.value)
                        val firebaseUser = User.from(json)

                        checkForUserDetailsAndUpdateLocal()

                        validDevice = (firebaseUser.Devices ?: mutableListOf()).count {
                            it.id == DeviceID
                        } > 0

                        if (!validDevice) {
                            deviceValidated.invoke(validDevice)
                            uninitialized()
                            removeDataObservation()
                            return@collect
                        }

                        if (json == null)
                            error.invoke(Exception("Database is null"))

                        val user = currentUserRepository.get()
                        val userClips = user?.Clips?.decrypt() ?: emptyList()
                        val firebaseClips = firebaseUser.Clips?.decrypt() ?: emptyList()

                        firebaseClips.minus(userClips).let { changed.invoke(it) }

                        if (bindDelete) {
                            val userDataClips = userClips.map { it.data }
                            val firebaseDataClips = firebaseClips.map { it.data }
                            userDataClips.minus(firebaseDataClips).let { if (it.isNotEmpty()) mainThread { removed.invoke(it) } }
                            if (firebaseDataClips.isEmpty() && userDataClips.isNotEmpty()) {
                                removedAll.invoke()
                            }
                        }

                        currentUserRepository.update(UserEntity.from(firebaseUser))
                    }
                    is ValueEventResponse.Cancelled -> {
                        error.invoke(result.error.toException())
                        HVLog.d("onError")
                    }
                }
            }
        }
    }

    /**
     * The method will update the local values needed for
     * later purposes.
     */
    private suspend fun checkForUserDetailsAndUpdateLocal() {
        HVLog.d()
        val user = currentUserRepository.get()
        APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())
        user?.LicenseStrategy?.let { licenseStrategy.postValue(it) }
    }

    // This might be not needed because all clips are not null by default or they
    // are mapped to not null :P
    @Suppress("SENSELESS_COMPARISON")
    @Deprecated("Must not be used")
    private suspend fun validateUser(): Boolean {
        val user = currentUserRepository.get()
        for (clip in user?.Clips ?: listOf()) {
            // This will be valid if user suppose manually remove a random node
            // which makes the tree inconsistent.
            if (clip == null) {
                HVLog.d(m = "Inconsistent data detected")
                mainThread { inconsistentDataListener?.invoke() }
                return false
            }
        }
        return true
    }

    private suspend fun removeUserDetailsAndUpdateLocal() {
        HVLog.d()
        val user = currentUserRepository.get()
        currentUserRepository.remove()
        licenseStrategy.postValue(LicenseType.Invalid)
        APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())
    }

    override fun removeDataObservation() {
       Coroutines.io {
           HVLog.d()
           if (isObservingChanges()) {
               job?.cancel()
               removeUserDetailsAndUpdateLocal()
           }
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