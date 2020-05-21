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


        val map = HashMap<ClipTag, String>()

        map[ClipTag.EMAIL] = "developerkp16@gmail.com"
        map[ClipTag.DATE] = "value"

        val joinToString = map.flatMap { pair ->
            ArrayList<String>().apply {
                add("${pair.key.name}${App.PAIR_SEPARATOR}${pair.value}")
            }
        }.joinToString(separator = App.ITEM_SEPARATOR) { data -> data }


        val associate = "URL:https://docs.microsoft.com/en-in/aspnet/core/blazor/templates?view=aspnetcore-3.1".split(App.ITEM_SEPARATOR).associate { string ->
            val pair = string.split(App.PAIR_SEPARATOR)
            if (!pair[0].isNullOrBlank())
                Pair(com.kpstv.xclipper.data.model.ClipTag.valueOf(pair[0]), pair[1])
            else
                Pair(com.kpstv.xclipper.data.model.ClipTag.EMPTY, "value")
        }

        println(joinToString)
        println(associate)

        /* val PHONE_PATTERN_REGEX = "(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}"
         val EMAIL_PATTERN_REGEX = "([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)"
         val URL_PATTERN_REGEX = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"


         val pattern = URL_PATTERN_REGEX.toRegex()

         val test = """
             test@123KP +91 (123) 456-7890


             http://kaustubhpatange.github.io

             https://google.com

             https://www.helloworld.com

             developerkp16@gmail.com
         """.trimIndent()

         println(pattern.find(test)?.value)*/

        /*  val list = listOf<Pair<String,String>>(
              Pair("key1","value1"),
              Pair("key2","value3"),
              Pair("key3","value3"),
              Pair("key4","value4")

          )

           val flatMap = list.flatMap { data ->
               val s = ArrayList<String>()
               s.add(data.first +":"+ data.second)
               s
           }

           println(flatMap)*/

        /*  println("oZDQctb9RVvZonVWymzVm86H0OhQmo7ZmmS/jWfAd9pJiI+fL7aQWQ2EbsdRqEi5".Decrypt())*/
        /*  val time1 = Calendar.getInstance().time
          val time2 = DateConverter.toDateFromString("20200514183402")!!

          val secondsInMilli: Long = 1000
          val minutesInMilli = secondsInMilli * 60
          val hoursInMilli = minutesInMilli * 60
          val daysInMilli = hoursInMilli * 24

          val hours = (time1.time - time2.time)/(1000 * 60 * 60)

          if (hours == 0L) println("Got here")
         // val hours: Long = (time2 - time1) / daysInMilli

          var different = time1.time - time2.time

          println("Hours: ${different/hoursInMilli}")*/
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
