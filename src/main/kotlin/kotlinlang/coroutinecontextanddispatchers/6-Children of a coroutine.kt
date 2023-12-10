package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {

        val request= launch {
            launch(Job()) {
                println("job1: I run in my own Job and execute independently!")
                delay(1000L)
                println("job1: I am not affected by cancellation of the request")
            }

            launch {
                delay(100L)
                println("job2: I am a child of the request coroutine")
                delay(1000)
                println("job2: I will not execute this line if my parent request is cancelled")
            }
        }

        delay(500L)
        request.cancel()
        println("main: Who has survived request cancellation?")
        delay(1000) // delay the main thread for a second to see what happens
    }
}