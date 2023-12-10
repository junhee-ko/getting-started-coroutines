package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val a= async {
            println("a, ${Thread.currentThread().name}")
            1
        }
        val b = async {
            println("b, ${Thread.currentThread().name}")
            2
        }

        println("The answer is ${a.await()} and ${b.await()}, ${Thread.currentThread().name}")
    }
}