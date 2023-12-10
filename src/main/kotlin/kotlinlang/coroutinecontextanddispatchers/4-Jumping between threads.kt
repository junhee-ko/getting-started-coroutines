package kotlinlang.coroutinecontextanddispatchers

import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main() {
    newSingleThreadContext("Ctx1").use { ctx1 ->

            newSingleThreadContext("Ctx2").use { ctx2 ->

                runBlocking(ctx1) {
                    log("Started in ctx1")

                    withContext(ctx2){
                        log("Working in ctx")
                    }

                    log("Back to ctx1")
                }

            }
    }
}