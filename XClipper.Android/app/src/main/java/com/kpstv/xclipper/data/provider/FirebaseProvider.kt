package com.kpstv.xclipper.data.provider

import androidx.lifecycle.LiveData
import com.google.firebase.database.DatabaseException
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import java.lang.Exception

interface FirebaseProvider {
    /** Property will expose database initialization live data */
    fun isInitialized() : LiveData<Boolean>

    fun initialize(options: FBOptions?)
    fun isLicensed(): Boolean
    fun isValidDevice(): Boolean
    fun uploadData(unencryptedClip: Clip)
    fun addDevice(DeviceId: String, responseListener: ResponseListener<Unit>)
    fun removeDevice(DeviceId: String, responseListener: ResponseListener<Unit>)
    fun replaceData(unencryptedOldClip: Clip, unencryptedNewClip: Clip)
    fun deleteData(unencryptedClip: Clip)
    fun deleteMultipleData(unencryptedClips: List<Clip>)
    fun clearData()

    /**
     * @return An unencrypted list of clip model.
     */
    fun getAllClipData(block: (List<Clip>?) -> Unit)

    /**
     * Should be called only by one specific class, because this lambdas are stored
     * internally for some use cases.
     */
    fun observeDataChange(changed: (User?) -> Unit, error: (Exception) -> Unit, deviceValidated: (Boolean) -> Unit)
    fun removeDataObservation()
}