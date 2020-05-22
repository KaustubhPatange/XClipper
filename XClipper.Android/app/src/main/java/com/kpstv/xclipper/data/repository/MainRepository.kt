package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.Status
import com.kpstv.xclipper.extensions.UpdateType

interface MainRepository {
    fun saveClip(clip: Clip?)

    /**
     * This operation will save data to only local database.
     *
     * During processing it will create the data from clip.data parameter.
     */
    fun processClipAndSave(clip: Clip?)

    /**
     * This method will either update the clip or insert the clip.
     *
     * When the data is coming from firebase saving it directly i.e overwriting it
     * might loose our existing custom tags on it. So this approach only updates
     * data & time with the existing clip in database.
     *
     * If it does not exist then we will process and save it.
     *
     * @param Clip A clip which is contains encrypted data and no tags (usually coming from firebase).
     */
    fun updateClip(clip: Clip?, updateType: UpdateType = UpdateType.Text)

    fun deleteClip(clip: Clip)

    fun deleteMultiple(clips: List<Clip>)

    /**
     * Use this function when you are saving data to local as well as firebase
     * database.
     *
     * While saving to database it will delete an existing data (if present)
     * and then create new data from the given string and perform insert operation.
     */
    fun updateRepository(data: String?)

    fun getAllData(): List<Clip>

    /**
     * TODO: There is a wrong insertion of data
     *
     * Since save clip function does work on separate thread, synchronization
     * between these threads on quick insertion is creating uneven insertions.
     */
    fun validateData(onComplete: (Status) -> Unit)

    fun getAllLiveClip(): LiveData<List<Clip>>
}