package com.kpstv.xclipper.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.extensions.enumerations.FilterType
import com.kpstv.xclipper.extensions.listeners.RepositoryListener
import com.kpstv.xclipper.extensions.listeners.StatusListener

interface MainRepository {

    /**
     * This operation implements a direct save to the database.
     *
     * The save method it uses is a safe push. It only inserts the data
     * when the database doesn't contains it.
     *
     * Determination of existing data is done by "Clip.data" property.
     *
     * @param clip Incoming clip data to save
     */
    fun saveClip(clip: Clip?)

    /**
     * This operation will save data to only local database.
     *
     * During processing it will create the data from "clip.data" parameter.
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
     * @param clip A clip which is contains encrypted string data and no [Clip.tags] (usually coming from firebase).
     */
    fun updateClip(clip: Clip?, filterType: FilterType = FilterType.Text)

    /**
     * The function will change [Clip.isPinned] to the incoming value.
     */
    fun updatePin(clip: Clip?, isPinned: Boolean)

    fun deleteClip(clip: Clip)
    fun deleteClip(unencryptedData: String?)
    fun deleteLast()

    fun deleteMultiple(clips: List<Clip>)

    /**
     * Use this function when you are saving data to local as well as firebase
     * database.
     *
     * While saving to database it will delete an existing data (if present)
     * and then create new data from the given string and perform insert operation.
     */
    fun updateRepository(unencryptedData: String?)
    fun updateRepository(clip: Clip)

    fun getAllData(): List<Clip>
    suspend fun getData(unencryptedText: String): Clip?

    /**
     * Checks if the clip exist in the database.
     */
    fun checkForDuplicate(unencryptedData: String?, repositoryListener: RepositoryListener)
    fun checkForDuplicate(unencryptedData: String?, id: Int, repositoryListener: RepositoryListener)

    /**
     * This function will check if there is any data depending on this tag.
     */
    fun checkForDependent(tagName: String, repositoryListener: RepositoryListener)

    fun getDataSource(): LiveData<PagedList<Clip>>

    /**
     * Since save clip function does work on separate thread, synchronization
     * between these threads on quick insertion is creating uneven insertions.
     *
     * TODO: There is a wrong sequence insertion of data
     */
    fun validateData(statusListener: StatusListener)

    fun getAllLiveClip(): LiveData<List<Clip>>
}