package com.kpstv.xclipper.data.provider

import com.kpstv.xclipper.data.model.Clip

interface ClipProvider {

    /**
     * This function will create a new clip with the given data.
     */
    fun processClip(unencryptedData: String?) : Clip?

    /**
     * This function will create a new clip with same Id, Data.
     *
     * It will recreate time and combine tag values with auto-generated one.
     */
    fun processClip(clip: Clip?): Clip?
}