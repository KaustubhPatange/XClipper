package com.kpstv.xclipper.extensions.utils

import com.kpstv.xclipper.App.DATE_PATTERN_REGEX
import com.kpstv.xclipper.App.EMAIL_PATTERN_REGEX
import com.kpstv.xclipper.App.MAP_PATTERN_REGEX
import com.kpstv.xclipper.App.MARKDOWN_IMAGE_ONLY_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX
import com.kpstv.xclipper.App.PHONE_PATTERN_REGEX1
import com.kpstv.xclipper.App.URL_PATTERN_REGEX
import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.data.model.Dictionary
import com.kpstv.xclipper.extensions.small

class ClipUtils {
    companion object {
        fun determineTags(data: String?): List<ClipTagMap> {
            if (data.isNullOrBlank()) return listOf()

            val dictList = ArrayList<ClipTagMap>()

            /** Url pattern matcher */
            patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, dictList)

            // Ignore further patterns for image markdown.
            if (MARKDOWN_IMAGE_ONLY_REGEX.toRegex().matches(data))
                return dictList

            /** Matches the phone number pattern. */
            if (!patternAdder(PHONE_PATTERN_REGEX, data, ClipTag.PHONE, dictList)
            ) {
                /** If not matched by first pattern we will try second one.  */
                patternAdder(PHONE_PATTERN_REGEX1, data, ClipTag.PHONE, dictList)
            }

            /** Date pattern matcher */
            patternAdder(DATE_PATTERN_REGEX, data, ClipTag.DATE, dictList)

            /** Email pattern matcher */
            patternAdder(EMAIL_PATTERN_REGEX, data, ClipTag.EMAIL, dictList)

            /** Map pattern matcher */
            patternAdder(MAP_PATTERN_REGEX, data, ClipTag.MAP, dictList)

            return dictList
        }

        private fun patternAdder(pattern: String, data: String, tag: ClipTag, dictList: ArrayList<ClipTagMap>): Boolean {
            val regex: Regex = pattern.toRegex()
            if (regex.containsMatchIn(data)) {
                val results: Sequence<MatchResult> = regex.findAll(data)
                for (result: MatchResult in results) {
                    dictList.add(Dictionary(tag.small(), result.value))
                }
                if (results.any()) return true
            }
            return false
        }
    }
}