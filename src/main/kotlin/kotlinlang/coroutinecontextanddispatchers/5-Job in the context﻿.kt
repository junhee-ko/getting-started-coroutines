package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println("My Job is ${coroutineContext[Job]}")
    }
}