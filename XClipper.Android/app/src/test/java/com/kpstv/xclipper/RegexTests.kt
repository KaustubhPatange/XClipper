package com.kpstv.xclipper

import com.kpstv.xclipper.extensions.toLines
import org.junit.Assert.assertEquals
import org.junit.Test

class RegexTests {
    @Test
    fun `do not match date in url`() {
        val string = "https://scontent.whatsapp.net/v/t61.25591-34/120308555_1002718070242926_4309815678525329467_n.apk/WhatsApp.apk?ccb=2&_nc_sid=4a4126&_nc_ohc=0vVlc2LWUNMAX9jR25w&_nc_ht=scontent.whatsapp.net&oh=bdce78431fa93320df119a381fe15509&oe=5FB865C0\t20201119214044\t0\tdate;91-34/1203|phone;1002718070|url;https://scontent.whatsapp.net/v/t61.25591-34/120308555_1002718070242926_4309815678525329467_n.apk/WhatsApp.apk?ccb=2&_nc_sid=4a4126&_nc_ohc=0vVlc2LWUNMAX9jR25w&_nc_ht=scontent.whatsapp.net&oh=bdce78431fa93320df119a381fe15509&oe=5FB865C0"
        assertEquals(false, App.DATE_PATTERN_REGEX.toRegex().matches(string))
    }

    @Test
    fun `match such dates`() {
        val string = """
            01/02/2000
            1.1.2019
            1-1-2000
            01-1-2000
            01/01/2000
            31/01/2000
        """.trimIndent()
        var result = true
        string.toLines().forEach {
            result = result and App.DATE_PATTERN_REGEX.toRegex().matches(it)
        }
        assertEquals(true, result)
    }
}