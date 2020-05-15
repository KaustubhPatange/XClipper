package com.kpstv.xclipper

import com.kpstv.xclipper.data.converters.DateConverter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

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

    @Test
    fun list_test() {

        val time1 = Calendar.getInstance().time
        val time2 = DateConverter.toDateFromString("20200514183402")!!

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val hours = (time1.time - time2.time)/(1000 * 60 * 60)

        if (hours == 0L) println("Got here")
       // val hours: Long = (time2 - time1) / daysInMilli

        var different = time1.time - time2.time

        println("Hours: ${different/hoursInMilli}")
//  val date = (time1 - time2).div((minutesInMilli * 60))


      //  println("Date: " + Date(time1 - time2))
        /* val size = 3

         val list = ArrayList(listOf(1,2,3))
         list.add(4)
         list.removeFirst()

         println(list)*/
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
