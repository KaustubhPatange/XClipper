package com.kpstv.xclipper.data.provider

import com.google.firebase.database.DatabaseException
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.User
import java.lang.Exception

interface FirebaseProvider {
    fun isLicensed(): Boolean
    fun isValidDevice(): Boolean
    fun uploadData(clip: Clip)
    fun replaceData(oldClip: Clip, newClip: Clip)
    fun deleteData(clip: Clip)
    fun deleteMultipleData(clips: List<Clip>)
    fun observeDataChange(changed: (User?) -> Unit, error: (Exception) -> Unit, deviceValidated: (Boolean) -> Unit)
}