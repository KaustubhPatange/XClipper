package com.kpstv.xclipper

import org.junit.Test

class GenericTest {

    data class Person(private val name: String)

    @Test
    fun assertCheckClass() {
        val person1 = Person("John")
        val person2 = Person("Hannah")

        if (person1.javaClass == Person::class.java) {
            println("Classes are same")
        }else {
            println("Classes are different")
        }
    }
}