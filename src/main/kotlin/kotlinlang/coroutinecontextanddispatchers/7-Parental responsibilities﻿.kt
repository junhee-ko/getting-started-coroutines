package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val request = launch {
            repeat(3) { i ->
                launch  {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    println("[${Thread.currentThread().name}], Coroutine $i is done")
                }
            }
            println("[${Thread.currentThread().name}], request: I'm done and I don't explicitly join my children that are still active")
        }

        request.join()
        println("[${Thread.currentThread().name}], Now processing of the request is complete")
    }
}