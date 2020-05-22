package com.kpstv.xclipper.data.provider

import com.kpstv.license.Decrypt
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import java.util.*
import kotlin.collections.HashMap

class ClipProviderImpl : ClipProvider {

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

    override fun processClip(clip: Clip?): Clip? {
        if (clip == null) return null

        return Clip(
            id = clip.id,
            data = clip.data,
            time = Calendar.getInstance().time,
            tags = determineTags(clip.data?.Decrypt())
        )
    }

   companion object {
       fun determineTags(data: String?): Map<ClipTag, String> {
           if (data.isNullOrBlank()) return EnumMap(ClipTag::class.java)

           val map = HashMap<ClipTag, String>()

           patternAdder(PHONE_PATTERN_REGEX, data, ClipTag.PHONE, map)
           patternAdder(EMAIL_PATTERN_REGEX, data, ClipTag.EMAIL, map)
           patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, map)

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
}