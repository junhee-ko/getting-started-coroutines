package kotlinlang.composingsuspendingfunctions

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

fun main() {
    runBlocking {
        val time = measureTimeMillis {
            val one = doSomethingUsefulOne()
            val two = doSomethingUsefulTwo()
            println("The answer is ${one + two}")
        }

        println("Completed in $time")
    }
}