package com.kpstv.xclipper.data.provider

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kpstv.license.Encryption.Decrypt
import com.kpstv.xclipper.App.APP_MAX_DEVICE
import com.kpstv.xclipper.App.APP_MAX_ITEM
import com.kpstv.xclipper.App.DELAY_FIREBASE_SPAN
import com.kpstv.xclipper.App.DeviceID
import com.kpstv.xclipper.App.UID
import com.kpstv.xclipper.App.bindToFirebase
import com.kpstv.xclipper.App.getMaxConnection
import com.kpstv.xclipper.App.getMaxStorage
import com.kpstv.xclipper.App.gson
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.cloneToEntries
import com.kpstv.xclipper.extensions.decrypt
import com.kpstv.xclipper.extensions.encrypt
import com.kpstv.xclipper.extensions.listeners.FValueEventListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import com.kpstv.xclipper.extensions.mainThread
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

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

    /**
     * There is drastic problem when device is being added. The thing is lot's of
     * internal calls are made to [FBOptions], so during pushing device data to firebase
     * in [observeDataChange] listener it still returns the cache version of data where the
     * device is not added yet. Due to this device validation fails and causing
     * [DBConnectionProvider] to run [DBConnectionProvider.detachDataFromAll] which is
     * removing all the preference related to connection
     *
     * The only solution I found is to set [isDataChanging] to true to know if device/data
     * being added or not. In such case [handler] will not post it's [Runnable].
     */
    private var isDataChanging = false

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

            checkForUserDetailsAndUpdateLocal()

            if (list.size >= APP_MAX_DEVICE) {
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
                database.goOffline()
                isDataChanging = false
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
        database.goOnline()
        database.getReference(USER_REF).child(UID).child(CLIP_REF)
            .setValue(list.cloneToEntries()) { error, _ ->
                if (error == null) {
                    user?.Clips = list
                    Log.e(TAG, "Firebase: Success")
                } else
                    Log.e(TAG, "Error: ${error.message}")
                database.goOffline()
                isDataChanging = false
            }
    }

    override fun getAllClipData(block: (List<Clip>?) -> Unit) {
        workWithData(ValidationContext.ForceInvoke) {
            if (it)
                block.invoke(user?.Clips?.decrypt())
            else
                block.invoke(null)
            database.goOffline()
            isDataChanging = false
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
        database.goOnline()

        if (user == null || validationContext == ValidationContext.ForceInvoke) {
            isDataChanging = true
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
                        database.goOffline()
                        isDataChanging = false
                    }
                ))
        } else {
            block.invoke(true)
        }

    }

    private var cancelToken = false
    private var handler = Handler()
    private lateinit var valueListener: FValueEventListener
    override fun observeDataChange(
        changed: (User?) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit
    ) {
        cancelToken = false
        handler.post(object : Runnable {
            override fun run() {

                if (cancelToken) {
                    handler.removeCallbacks(this)
                    return
                }

                if (!dbConnectionProvider.isValidData()) {
                    error.invoke(Exception("Invalid account preference"))
                    loopRunnableAfterDelay()
                    return
                }

                if (isDataChanging) {
                    error.invoke(Exception("Data is changing"))
                    loopRunnableAfterDelay()
                    return
                }

                /** Automated initialization of firebase database*/
                if (isInitialized.value == false)
                    initialize(dbConnectionProvider.optionsProvider())

                if (!dbConnectionProvider.isValidData()) {
                    loopRunnableAfterDelay()
                    return
                }

                valueListener = FValueEventListener(
                    onDataChange = { snap ->
                        val json = gson.toJson(snap.value)
                        val gotUser = gson.fromJson(json, User::class.java)
                        if (gotUser == user) {
                            mainThread { error.invoke(Exception("Pulled out same data")) }
                            loopRunnableAfterDelay()
                            return@FValueEventListener
                        }

                        user = gotUser

                        /** Check for device validation*/
                        validDevice = (user?.Devices ?: mutableListOf()).count {
                            it.id == DeviceID
                        } > 0

                        /** Update the properties*/
                        checkForUserDetailsAndUpdateLocal()

                        /** Device validation is causing problem, so only invoke it when
                         *  device is not adding.*/
                        mainThread { deviceValidated.invoke(validDevice) }

                        if (json != null && validDevice)
                            mainThread { changed.invoke(user) }
                        else if (json == null)
                            mainThread { error.invoke(Exception("Database is null")) }
                        else
                            mainThread { error.invoke(Exception("Unknown error occurred")) }

                        if (!validDevice) isInitialized.postValue(false)

                        database.goOffline()
                        loopRunnableAfterDelay()
                    },
                    onError = {
                        error.invoke(it.toException())

                        database.goOffline()
                        loopRunnableAfterDelay()
                    }
                )

                database.goOnline()
                database.getReference(USER_REF).child(UID)
                    .addListenerForSingleValueEvent(valueListener)
            }
        })
    }

    /**
     * An extension method to re-post the runnable.
     */
    fun Runnable.loopRunnableAfterDelay() {
        Timer("loopObservation").schedule(DELAY_FIREBASE_SPAN) {
            mainThread { handler.post(this@loopRunnableAfterDelay) }
        }
    }

    /**
     * The method will update the local values needed for
     * later purposes.
     */
    private fun checkForUserDetailsAndUpdateLocal() {
        APP_MAX_DEVICE = if (user?.TotalConnection != 0) user?.TotalConnection ?: getMaxConnection(
            isLicensed()
        ) else getMaxConnection(isLicensed())
        APP_MAX_ITEM = if (user?.MaxItemStorage != 0) user?.MaxItemStorage ?: getMaxStorage(
            isLicensed()
        ) else getMaxStorage(isLicensed())
        /*APP_MAX_DEVICE = user?.TotalConnection ?: getMaxConnection(isLicensed())
        APP_MAX_ITEM = user?.MaxItemStorage ?: getMaxStorage(isLicensed())*/
    }

    override fun removeDataObservation() {
        if (::database.isInitialized && ::valueListener.isInitialized) {
            /*database.getReference(USER_REF).child(UID)
                .removeEventListener(valueListener)*/
            cancelToken = true
        }
    }

    private enum class ValidationContext {
        Default,
        ForceInvoke
    }
}