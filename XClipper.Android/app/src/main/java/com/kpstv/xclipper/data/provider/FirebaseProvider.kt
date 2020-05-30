package com.kpstv.xclipper.data.provider

import com.google.firebase.database.DatabaseException
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.Device
import com.kpstv.xclipper.data.model.User
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.ResponseListener
import java.lang.Exception

interface FirebaseProvider {
    fun isLicensed(): Boolean
    fun isValidDevice(): Boolean
    fun uploadData(clip: Clip)
    fun addDevice(DeviceId: String, responseListener: ResponseListener<Unit>)
    fun removeDevice(DeviceId: String, responseListener: ResponseListener<Unit>)
    fun replaceData(oldClip: Clip, newClip: Clip)
    fun deleteData(clip: Clip)
    fun deleteMultipleData(clips: List<Clip>)
    fun clearData()
    fun getAllClipData(block: (List<Clip>?) -> Unit)
    fun observeDataChange(changed: (User?) -> Unit, error: (Exception) -> Unit, deviceValidated: (Boolean) -> Unit)
}