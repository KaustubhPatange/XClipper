package com.kpstv.xclipper.data.provider

import android.util.Log
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import java.util.*
import kotlin.collections.HashMap

class ClipProviderImpl : ClipProvider {

    private val TAG = javaClass.simpleName

    override fun processClip(data: String?): Clip? {
        if (data.isNullOrBlank()) return null
        data.let {
            return Clip(
                data = data.Encrypt(),
                time = Calendar.getInstance().time,
                tags = determineTags(it)
            )
        }
    }

    private fun determineTags(data: String): Map<ClipTag, String> {
        val map = HashMap<ClipTag, String>()

        patternAdder(PHONE_PATTERN_REGEX, data, ClipTag.PHONE, map)
        patternAdder(EMAIL_PATTERN_REGEX, data, ClipTag.EMAIL, map)
        patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, map)

        Log.e(TAG, "Map size: ${map.size}")

        return map
    }

    private fun patternAdder(
        pattern: String,
        data: String,
        tag: ClipTag,
        map: HashMap<ClipTag, String>
    ) {
        pattern.toRegex().let {
            if (it.containsMatchIn(data))
                map[tag] = it.find(data)?.value!!
        }
    }
}