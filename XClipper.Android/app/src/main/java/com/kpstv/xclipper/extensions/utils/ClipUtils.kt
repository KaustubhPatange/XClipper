package com.kpstv.xclipper.extensions.utils

import com.kpstv.xclipper.App.DATE_PATTERN_REGEX
import com.kpstv.xclipper.App.DATE_PATTERN_REGEX1
import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.MAP_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX1
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.small

class ClipUtils {
    companion object {
        fun determineTags(data: String?): Map<String, String> {
            if (data.isNullOrBlank()) return HashMap()

            val map = HashMap<String, String>()

            /** Matches the phone number pattern. */
            if (!patternAdder(PHONE_PATTERN_REGEX, data, ClipTag.PHONE, map)
            ) {
                /** If not matched by first pattern we will try second one.  */
                patternAdder(PHONE_PATTERN_REGEX1, data, ClipTag.PHONE, map)
            }

            /** Date pattern matcher */
            if (!patternAdder(DATE_PATTERN_REGEX, data, ClipTag.DATE, map))
                patternAdder(DATE_PATTERN_REGEX1, data, ClipTag.DATE, map)

            /** Email pattern matcher */
            patternAdder(EMAIL_PATTERN_REGEX, data, ClipTag.EMAIL, map)

            /** Url pattern matcher */
            patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, map)

            /** Map pattern matcher */
            patternAdder(MAP_PATTERN_REGEX, data, ClipTag.MAP, map)

            return map
        }

        private fun patternAdder(
            pattern: String,
            data: String,
            tag: ClipTag,
            map: HashMap<String, String>
        ): Boolean {
            pattern.toRegex().let {
                if (it.containsMatchIn(data)) {
                    map[tag.small()] = it.find(data)?.value!!
                    return true
                }
            }
            return false
        }
    }
}