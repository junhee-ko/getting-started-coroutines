package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.*

private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")


class Activity {

    private val mainScope = CoroutineScope(Dispatchers.Default) // use Default for test purposes

    fun destroy() {
        mainScope.cancel()
    }

    fun doSomething() {
        // launch ten coroutines for a demo, each working for a different time
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, ... etc
                log("Coroutine $i is done")
            }
        }
    }
}

fun main() = runBlocking<Unit> {
    val activity = Activity()
    activity.doSomething() // run test function
    log("Launched coroutines")

    delay(500L)

    log("Destroying activity!")
    activity.destroy() // cancels all coroutines

    delay(1000) // visually confirm that they don't work
}