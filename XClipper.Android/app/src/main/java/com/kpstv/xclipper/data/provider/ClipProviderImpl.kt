package com.kpstv.xclipper.data.provider

import androidx.lifecycle.MutableLiveData
import com.kpstv.license.Decrypt
import com.kpstv.license.Encrypt
import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.MAP_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.Clip
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.small
import java.util.*
import kotlin.collections.HashMap

class ClipProviderImpl : ClipProvider {

    override fun processClip(unencryptedData: String?): Clip? {
        if (unencryptedData.isNullOrBlank()) return null
        unencryptedData.let {
            return Clip(
                data = unencryptedData.Encrypt(),
                time = Calendar.getInstance().time,
                tags = determineTags(it)
            )
        }
    }

    override fun processClip(clip: Clip?): Clip? {
        if (clip == null) return null
        val tagMap: Map<String, String> = clip.tags ?: HashMap()
        return Clip(
            id = clip.id,
            data = clip.data,
            time = Calendar.getInstance().time,
            tags = tagMap + determineTags(clip.data?.Decrypt())
        )
    }

   companion object {
       fun determineTags(data: String?): Map<String, String> {
           if (data.isNullOrBlank()) return HashMap()

           val map = HashMap<String, String>()

           patternAdder(PHONE_PATTERN_REGEX, data, ClipTag.PHONE, map)
           patternAdder(EMAIL_PATTERN_REGEX, data, ClipTag.EMAIL, map)
           patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, map)
           patternAdder(MAP_PATTERN_REGEX, data, ClipTag.MAP, map)

           return map
       }

       private fun patternAdder(
           pattern: String,
           data: String,
           tag: ClipTag,
           map: HashMap<String, String>
       ) {
           pattern.toRegex().let {
               if (it.containsMatchIn(data))
                   map[tag.small()] = it.find(data)?.value!!
           }
       }
   }
}