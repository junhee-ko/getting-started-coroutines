package kotlinlang.coroutinesbasics

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main () {
    println("1")
    runBlocking {
        println("2")
        doWorld()
        println("3")
    }
    println("4")
}

private suspend fun doWorld() {
    println("11")
    coroutineScope {
        launch {
            delay(1000L)
            println("World")
        }
        println("Hello")
    }
    println("22")
}