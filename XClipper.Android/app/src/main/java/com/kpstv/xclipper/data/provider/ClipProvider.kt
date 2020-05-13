package com.kpstv.xclipper.data.provider

import com.kpstv.xclipper.data.model.Clip

interface ClipProvider {
    fun processClip(data: String?) : Clip?
}