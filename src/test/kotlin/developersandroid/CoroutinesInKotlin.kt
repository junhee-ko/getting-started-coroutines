package developersandroid

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CoroutinesInKotlin {

    @Test
    fun `use coroutines`() {
        repeat(3) {
            GlobalScope.launch {
                println("Hi from ${Thread.currentThread()}")
            }
        }

        Thread.sleep(3000)
    }

    @Test
    fun `A word about runBlocking`() {
        runBlocking {
            val num1: Double = getValue()
            val num2: Double = getValue()
            println("result of num1 + num2 is ${num1 + num2}")
        }
    }

    @Test
    fun `A word about runBlocking with async`() {
        runBlocking {
            val num1: Deferred<Double> = async { getValue() }
            val num2: Deferred<Double> = async { getValue() }
            println("result of num1 + num2 is ${num1.await() + num2.await()}")
        }
    }

    // Whenever a function calls another suspend function, then it should also be a suspend function.
    suspend fun getValue(): Double {
        println("entering getValue() at ${LocalDateTime.now()}")
        delay(3000) // suspend function
        println("leaving getValue() at ${LocalDateTime.now()}")

        return Math.random()
    }

}