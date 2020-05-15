package com.kpstv.xclipper.data.provider

import com.kpstv.license.Encrypt
import com.kpstv.xclipper.data.model.Clip
import java.util.*

class ClipProviderImpl : ClipProvider {

    override fun processClip(data: String?): Clip? {
        if (data.isNullOrBlank()) return null
        data.let {
            return Clip(
                data = data.Encrypt(),
                time = Calendar.getInstance().time
            )
        }
    }
}