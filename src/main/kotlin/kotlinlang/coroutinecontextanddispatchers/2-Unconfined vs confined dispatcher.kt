package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

    runBlocking {
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            println("Unconfined      : I'm working in thread ${Thread.currentThread()}")
            delay(500)
            println("Unconfined      : After delay in thread ${Thread.currentThread()}")
        }
        launch { // context of the parent, main runBlocking coroutine
            println("main runBlocking: I'm working in thread ${Thread.currentThread()}")
            delay(1000)
            println("main runBlocking: After delay in thread ${Thread.currentThread()}")
        }
    }
}