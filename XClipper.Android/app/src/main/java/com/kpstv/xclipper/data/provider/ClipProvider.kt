package com.kpstv.xclipper.data.provider

import com.kpstv.xclipper.data.model.Clip

interface ClipProvider {

    /**
     * This function will create a new clip with the given data.
     */
    fun processClip(data: String?) : Clip?

    /**
     * This function will create a new clip with same Id, Data.
     * It will recreate time and tag values.
     */
    fun processClip(clip: Clip?): Clip?
}