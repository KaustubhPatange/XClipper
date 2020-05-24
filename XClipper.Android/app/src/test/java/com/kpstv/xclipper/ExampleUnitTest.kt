package com.kpstv.xclipper

import android.util.Log
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.model.ClipTag
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val TAG = javaClass.simpleName

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    enum class ClipTag {
        PHONE, DATE, URL, EMAIL, EMPTY
    }

    data class User (
        val id: Int,
        val text: String
    ) {
        var list: List<Int>? = null

        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + text.hashCode()
            result = 31 * result + (list?.hashCode() ?: 0)
            return result
        }
    }

    @Test
    fun list_test() {

        val string = """
            [
        {
            "word": "bonjour",
            "phonetic": "/bänˈZHo͝or/",
            "origin": "French, literally ‘good day’.",
            "meaning": {
                "exclamation": [
                    {
D/OkHttp:                     "definition": "A French greeting used when meeting during the daytime.",
                        "example": "there you are, bonjour!"
                    }
                ],
                "noun": [
                    {
                        "definition": "An utterance of “bonjour”",
                        "example": "be sure to greet him with a hearty “bonjour”"
                    }
                ]
            }
        }
    ]
        """.trimIndent()
        "\"definition\":\\s\".*?\"".toRegex().let {
            if (it.containsMatchIn(string)) {
                val value = it.find(string)?.value ?: ""
                println(value.split("\"")[3])
            }
        }

      /*  val user1 = User(1, "text")
        user1.list = listOf(1,2,3)
        val user2 = User(1, "text")


        println(user1 == user2)*/

    }

    fun printDifference(startDate: Date, endDate: Date) {
        //milliseconds
        var different = endDate.time - startDate.time
        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different %= daysInMilli
        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )
    }
}
