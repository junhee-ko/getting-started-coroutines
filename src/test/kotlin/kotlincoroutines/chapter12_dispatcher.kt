package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Chapter12 {

    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html
    @Test
    fun `Default dispatcher 는 CPU 개수와 동일한 수의 스레드 풀을 가지고 있다`() {
        runBlocking {
            repeat(100) {
                launch(Dispatchers.Default) {

                    // 복잡한 연산
                    val maxOrNull = List(1000) { Random.nextLong() }.maxOrNull()

                    log("max value: ${maxOrNull}")
                }
            }
        }
    }

    @Test
    fun `디스페처가 같은 스레드 풀을 사용하지만 같은 시간에 특정 수 이상의 스레드를 사용하지 못하도록 제한할 수 있다`() {
        runBlocking {

            val limitedParallelism: CoroutineDispatcher = Dispatchers.Default
                .limitedParallelism(5)

            repeat(100) {
                launch(limitedParallelism) {

                    // 복잡한 연산
                    val maxOrNull = List(1000) { Random.nextLong() }.maxOrNull()

                    log("max value: ${maxOrNull}")
                }
            }
        }
    }

    // Dispatchers.IO 와 Dispatchers.Default 는 같은 스레드 풀을 공유
    @Test
    fun `같은 시간에 50 개가 넘는 스레드를 사용하는 경우`() {
        runBlocking {
            val time = measureTimeMillis {
                coroutineScope {
                    repeat(50) {
                        launch(Dispatchers.IO) {
                            Thread.sleep(1000)
                            log("done")
                        }
                    }
                }
            }

            log("time: $time")
        }
    }

    // withContext 에서, Dispatchers.Default 의 한도가 아니라 Dispatchers.IO 의 한도로 적용된다.
    @Test
    fun `스레드 한도는 독립적이다`() {
        runBlocking {
            coroutineScope {
                launch(Dispatchers.Default) {
                    log("launch")
                    withContext(Dispatchers.IO) {
                        log("withContext")
                    }
                }
            }
        }
    }

    @Test
    fun `디스페처의 한도는 서로 무관하다`() {
        runBlocking {
            val dispatcher1 = Dispatchers.IO
            printCoroutinesTime(dispatcher1)

            val dispatcher2 = Dispatchers.IO.limitedParallelism(100)
            printCoroutinesTime(dispatcher2)
        }
    }

    private suspend fun printCoroutinesTime(dispatcher: CoroutineDispatcher) {
        val time = measureTimeMillis {
            coroutineScope {
                repeat(100) {
                    launch(dispatcher) {
                        Thread.sleep(1000)
                    }
                }
            }
        }

        log("took: $time")
    }

    @Test
    fun `정해진 수의 스레드 풀을 가진 디스페처`() {
        val NUMBER_OF_THREADS = 20
        val asCoroutineDispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
            .asCoroutineDispatcher()
    }

    @Test
    fun `동일 시간에 다수의 스레드가 공유 상태를 변경하는 경우`() {
        var i = 0

        runBlocking {
            repeat(1000){
                launch(Dispatchers.IO) {
                    i++
                }
            }

            delay(1000)
            log("result: ${i}")
        }
    }

    @Test
    fun `싱그스레드를 가진 디스페처를 사용하면 해결 할 수 있다`() {
        Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher()

        // or

        newSingleThreadContext("jko")

        // or

        Dispatchers.Default.limitedParallelism(1)
    }

    @Test
    fun `Unconfined 디스페처는 스레드를 바꾸지 않는다`() {

        runBlocking {
            withContext(newSingleThreadContext("jko-thread-1")){
                var continuation: Continuation<Unit>? = null

                launch(newSingleThreadContext("jko-tread-2")) {
                    delay(1000)
                    continuation?.resume(Unit)
                }
            }
        }
    }
}