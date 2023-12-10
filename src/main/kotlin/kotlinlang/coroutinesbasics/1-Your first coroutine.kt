package kotlinlang.coroutinesbasics

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    println("1")

    runBlocking {
        launch {
            delay(1000L)
            println("World!")
        }

        println("Hello")
    }

    println("2")
}