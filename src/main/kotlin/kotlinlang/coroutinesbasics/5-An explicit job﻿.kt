package kotlinlang.coroutinesbasics

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val job = launch {
            delay(5000L)
            println("World")
        }
        println("Hello")
        job.join()
        println("Done")
    }
}