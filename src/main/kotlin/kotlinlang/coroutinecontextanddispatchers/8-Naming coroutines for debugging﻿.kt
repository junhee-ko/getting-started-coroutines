package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")


fun main() {
    runBlocking {
        log("Started main coroutine")

        val v1 = async(CoroutineName("v1coroutine")) {
            delay(500)
            log("Computing v1")
            252
        }

        val v2 = async(CoroutineName("v2coroutine")) {
            delay(1000)
            log("Computing v2")
            6
        }

        log("before calculate")
        log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
        log("after calculate")
    }
}