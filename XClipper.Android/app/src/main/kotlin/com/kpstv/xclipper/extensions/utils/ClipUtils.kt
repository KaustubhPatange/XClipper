package com.kpstv.xclipper.extensions.utils

import com.kpstv.xclipper.data.model.ClipTag
import com.kpstv.xclipper.extensions.ClipTagMap
import com.kpstv.xclipper.data.model.Dictionary
import com.kpstv.xclipper.extensions.small

class ClipUtils {
    companion object {
        private const val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}" // matches international numbers
        private const val PHONE_PATTERN_REGEX1 = "(\\+[\\d-]{1,4})[\\s\\.]?(\\d{5})[\\s\\.]?(\\d{5})" // matches some specific number patterns
        private const val EMAIL_PATTERN_REGEX =
            "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
        private const val URL_PATTERN_REGEX =
            "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
        private const val MAP_PATTERN_REGEX =
            "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)°?\\s*(N|S|E|W)?,\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)°?\\s*(N|S|E|W)?"
        private const val DATE_PATTERN_REGEX =
            "(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))\$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})"
        private const val MARKDOWN_IMAGE_ONLY_REGEX = "^(!\\[)(.*?)(])(\\((https?://.*?)\\))$"

        fun isMarkdownImage(data: String) : Boolean = (MARKDOWN_IMAGE_ONLY_REGEX.toRegex().matches(data))
        fun getMarkdownImageUrl(data: String) : String? {
            return MARKDOWN_IMAGE_ONLY_REGEX.toRegex().matchEntire(data)?.groupValues?.get(5)
        }

        fun determineTags(data: String): List<ClipTagMap> {
            if (data.isNullOrBlank()) return listOf()

            val dictList = ArrayList<ClipTagMap>()

            /** Url pattern matcher */
            patternAdder(URL_PATTERN_REGEX, data, ClipTag.URL, dictList)

            // Ignore further patterns for image markdown.
            if (isMarkdownImage(data))
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