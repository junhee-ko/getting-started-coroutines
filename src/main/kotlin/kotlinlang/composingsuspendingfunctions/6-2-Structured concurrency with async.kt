package kotlinlang.composingsuspendingfunctions

import kotlinx.coroutines.*
import java.lang.ArithmeticException
import kotlin.system.measureTimeMillis

private suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one: Deferred<Int> = async<Int> {
        try {
            delay(Long.MAX_VALUE) // Emulates very long computation
            42
        } finally {
            println("First child was cancelled")
        }
    }

    val two: Deferred<Int> = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }

    one.await() + two.await()
}

fun main() {
    runBlocking {
        try {
            failedConcurrentSum()
        }catch (e: ArithmeticException){
            println(e)
        }
    }
}