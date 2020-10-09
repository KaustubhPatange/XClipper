package com.kpstv.xclipper.data.provider

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.LicenseType
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.listeners.ResponseListener

interface FirebaseProvider {
    /** Property will expose database initialization live data */
    fun isInitialized(): LiveData<Boolean>

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
     * @return [LicenseType] from current firebase configuration.
     */
    fun getLicenseStrategy(): LiveData<LicenseType>

    /**
     * @return An unencrypted list of clip model.
     */
    fun getAllClipData(block: (List<Clip>?) -> Unit)

    /**
     * Should be called only by one specific class, because this lambdas are stored
     * internally for some use cases.
     */
    fun isObservingChanges(): Boolean
    fun observeDataChange(
        changed: (Clip?) -> Unit,
        removed: (String?) -> Unit,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit,
        inconsistentData: SimpleFunction
    )

    fun removeDataObservation()
}