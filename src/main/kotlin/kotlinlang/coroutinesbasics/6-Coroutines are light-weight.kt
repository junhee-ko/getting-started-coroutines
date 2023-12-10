package kotlinlang.coroutinesbasics

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

fun main() {
    runBlocking {
        repeat(50_000){
            launch {
                delay(5000L)
                println(".")
            }
        }
    }
}

//fun main() {
//    repeat(50_000) {
//        thread {
//            Thread.sleep(5000L)
//            println(".")
//        }
//    }
//}