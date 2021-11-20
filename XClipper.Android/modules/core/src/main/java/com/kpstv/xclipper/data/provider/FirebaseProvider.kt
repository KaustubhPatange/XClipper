package com.kpstv.xclipper.data.provider

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.enumerations.LicenseType
import com.kpstv.xclipper.extensions.SimpleFunction
import com.kpstv.xclipper.extensions.listeners.ResponseResult

interface FirebaseProvider {
    /** Property will expose database initialization live data */
    fun isInitialized(): LiveData<Boolean>

    fun initialize(options: FBOptions?, notifyInitialization: Boolean = true)
    fun uninitialized()
    suspend fun isLicensed(): Boolean
    fun isValidDevice(): Boolean
    suspend fun uploadData(unencryptedClip: Clip)
    suspend fun addDevice(DeviceId: String): ResponseResult<Unit>
    suspend fun removeDevice(DeviceId: String): ResponseResult<Unit>
    suspend fun replaceData(unencryptedOldClip: Clip, unencryptedNewClip: Clip)
    suspend fun deleteData(unencryptedClip: Clip)
    suspend fun deleteMultipleData(unencryptedClips: List<Clip>)
    suspend fun clearData()

    /**
     * @return [LicenseType] from current firebase configuration.
     */
    fun getLicenseStrategy(): LiveData<LicenseType>

    /**
     * @return An unencrypted list of clip model.
     */
    suspend fun getAllClipData(): List<Clip>?

    /**
     * Should be called only by one specific class, because this lambdas are stored
     * internally for some use cases.
     */
    fun isObservingChanges(): Boolean
    fun observeDataChange(
        changed: (List<Clip>) -> Unit,
        removed: (List<String>?) -> Unit,
        removedAll: SimpleFunction,
        error: (Exception) -> Unit,
        deviceValidated: (Boolean) -> Unit,
        inconsistentData: SimpleFunction
    )

    fun removeDataObservation()
}