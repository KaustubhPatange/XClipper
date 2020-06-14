package com.kpstv.xclipper.extensions.utils

import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.MAP_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.small
import kotlin.collections.HashMap

class ClipUtils  {
   companion object {
       fun determineTags(data: String?): Map<String, String> {
           if (data.isNullOrBlank()) return HashMap()

           val map = HashMap<String, String>()

           patternAdder(
               PHONE_PATTERN_REGEX,
               data,
               ClipTag.PHONE,
               map
           )
           patternAdder(
               EMAIL_PATTERN_REGEX,
               data,
               ClipTag.EMAIL,
               map
           )
           patternAdder(
               URL_PATTERN_REGEX,
               data,
               ClipTag.URL,
               map
           )
           patternAdder(
               MAP_PATTERN_REGEX,
               data,
               ClipTag.MAP,
               map
           )

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