package kotlinlang.composingsuspendingfunctions

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private suspend fun doSomethingUsefulOne(): Int {
    println("1")
    delay(1000L)
    println("2")
    return 13
}

private suspend fun doSomethingUsefulTwo(): Int {
    println("3")
    delay(1000L)
    println("4")
    return 29
}

@OptIn(DelicateCoroutinesApi::class)
private fun somethingUsefulOneAsync(): Deferred<Int> = GlobalScope.async {
    doSomethingUsefulOne()
}

@OptIn(DelicateCoroutinesApi::class)
private fun somethingUsefulTwoAsync(): Deferred<Int> = GlobalScope.async {
    doSomethingUsefulTwo()
}

fun main() {
    val time = measureTimeMillis {
        // we can initiate async actions outside of a coroutine
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()

        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")
}