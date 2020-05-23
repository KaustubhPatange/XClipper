package com.kpstv.xclipper

import android.util.Log
import com.kpstv.xclipper.data.converters.TagConverter
import com.kpstv.xclipper.data.model.ClipTag
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
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

    @Test
    fun list_test() {

        val map1 = mutableMapOf(
            "Email" to "developerkp16@gmail.com",
            "date" to "value",
            "key1" to "value1",
            "key2" to "value2"
        )
        val map2 = mutableMapOf(
            "key2" to "value2",
            "key3" to "value3",
            "key4" to "value4"
        )

        val map3: Map<String, String>?

        //println((map1 + map2 + map3))

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
