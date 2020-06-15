package com.kpstv.xclipper.data.provider

import com.kpstv.xclipper.data.localized.FBOptions
import com.kpstv.xclipper.extensions.listeners.ResponseListener

interface DBConnectionProvider {
    fun isValidData(): Boolean
    fun processResult(data: String?, responseListener: ResponseListener<FBOptions>)

    /**
     * This will save all the FBOptions to preference as well as
     * set values of all required properties in App.kt
     */
    fun saveOptionsToAll(options: FBOptions)

    /**
     * This will remove all the preference related to Firebase Connection,
     * also it will make settings from App.kt empty.
     */
    fun detachDataFromAll()
    fun optionsProvider(): FBOptions?
    fun loadDataFromPreference()
}