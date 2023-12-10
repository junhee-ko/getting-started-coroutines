package kotlinlang.composingsuspendingfunctions

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

private suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    one.await() + two.await()
}

fun main() {
    runBlocking {
        val time = measureTimeMillis {
            println("The answer is ${concurrentSum()}")
        }
        println("Completed in $time ms")
    }
}