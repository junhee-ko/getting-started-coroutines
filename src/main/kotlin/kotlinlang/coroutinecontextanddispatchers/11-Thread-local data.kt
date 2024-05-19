package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun main() {
    val threadLocal = ThreadLocal<String>()
    threadLocal.set("main")

    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")

    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    val threadContextElement: ThreadContextElement<String> = threadLocal.asContextElement("new main")
    val coroutineContext: CoroutineContext = dispatcher + threadContextElement

    runBlocking(coroutineContext) {
        println("Before launch in runBlocking, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")

        launch {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
            delay(3000L)
            println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        }

        println("After launch in runBlocking, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }

    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}
