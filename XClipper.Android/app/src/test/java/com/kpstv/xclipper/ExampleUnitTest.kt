package com.kpstv.xclipper

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @ExperimentalStdlibApi
    @Test
    fun list_test() {

        val list = listOf(1,2,3)
        println(list.first())

       /* val size = 3

        val list = ArrayList(listOf(1,2,3))
        list.add(4)
        list.removeFirst()

        println(list)*/
    }
}
