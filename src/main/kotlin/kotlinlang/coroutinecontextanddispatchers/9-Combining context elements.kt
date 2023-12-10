package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        launch(Dispatchers.Default + CoroutineName("jko-test")) {
            println("[${Thread.currentThread().name}] I'm working in thread")
        }
    }
}