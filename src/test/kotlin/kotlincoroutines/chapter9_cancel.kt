package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

class Chapter9 {

    @Test
    fun `cancel and join`() {
        runBlocking {
            val job = launch {
                repeat(1_000) {
                    delay(200)
                    log("printing ${it}")
                }
            }

            delay(1100)
            job.cancel()
            job.join()  // 취소 과정이 완료되는 걸 기다리기 위해 사용
            log("DONE")
        }
    }

    @Test
    fun `join 호출이 없으면`() {
        runBlocking {
            val job = launch {
                repeat(1_000) {
                    delay(100)
                    Thread.sleep(500) // 오래 걸리는 작업
                    log("printing $it")
                }
            }

            delay(800)
            job.cancel()
            log("Cancelled successfully")
        }
    }

    @Test
    fun `join 호출이 있으면, 코루틴이 취소를 마칠 때까지 중단된다`() {
        runBlocking {
            val job = launch {
                repeat(1_000) {
                    delay(100)
                    Thread.sleep(100) // 오래 걸리는 작업
                    log("printing $it")
                }
            }

            delay(1000)
            job.cancel()
            job.join()
            log("Cancelled successfully")
        }
    }

    @Test
    fun `cancel 과 join 을 함께 호출할 수 있는 방법`() {
        runBlocking {
            val job = launch {
                repeat(1_000) {
                    delay(100)
                    Thread.sleep(100) // 오래 걸리는 작업
                    log("printing $it")
                }
            }

            delay(1000)
            job.cancelAndJoin()
            log("Cancelled successfully")
        }
    }

    // Job 팩토리 함수로 생성된 잡
    @Test
    fun `Job 에 딸린 수많은 코루틴을 한 번에 취소`() {
        runBlocking {
            val job = Job()

            launch(job) {
                repeat(1_000) { i ->
                    delay(200)
                    log("printing: ${i}")
                }
            }

            delay(1100)
            job.cancelAndJoin()
            log("DONE")
        }
    }

    @Test
    fun `잡이 취소되면 Cancelling 상태로 바뀌고, 첫 번째 중단점에서 예외를 던진다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                try {
                    repeat(1_000) { i ->
                        delay(200)
                        log("printing ${i}")
                    }
                } catch (e: CancellationException) {
                    log(e)
                    throw e
                }
            }

            delay(1100)
            job.cancelAndJoin()
            log("cancelled")
            delay(1000)
        }
    }

    @Test
    fun `취소된 코루틴은 단지 멈추는 것이 아니라, 내부적으로 예외를 사용해서 취소하므로 finally 블록 안에서 모든 것을 정리할 수 있다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                try {
                    delay(2000)
                    log("DONE")
                } catch (e: CancellationException) {
                   log(e)
                } finally {
                    log("will be printed")
                }
            }

            delay(1000)
            job.cancelAndJoin()
        }
    }

    @Test
    fun `Cancelling 상태인 잡은 중단되거나 다른 코루틴을 시작하는 것은 불가능하다 - 중단하려고 하면 throw CancellationException`() {
        runBlocking {
            val job = Job()

            launch(job){
                try {
                    delay(2000)
                    log("Job is DONE")
                } finally {
                    log("Finally")
                    launch {
                        log("will not be printed")
                    }
                    delay(1000) // 예외 발생
                    log("will not be printed")
                }
            }

            delay(1000)
            job.cancelAndJoin()
            log("cancel done")
        }
    }

    @Test
    fun `코루틴이 이미 취쇠되었는데, 중단함수를 호출해야하는 경우는 withContext(NonCancellable) 를 사용하면 된다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                try{
                    delay(200)
                    log("Finished coroutine")
                } finally {
                    log("Finally")
                    withContext(NonCancellable){
                        delay(1000L)
                        log("Cleanup done")
                    }
                }
            }

            delay(100)
            job.cancelAndJoin()
            log("DONE")
        }
    }

    @Test
    fun `invokeOnCompletion 은 잡이 Completed 나 Cancelled 와 같은 마지막 상태에 도달했을 때 호출될 핸들러를 지정하는 역할이다`() {
        runBlocking {
            val job = launch {
                delay(1000)
            }
            job.invokeOnCompletion { cause: Throwable? ->
                log("Finished, ${cause}")
            }
            delay(400)
            job.cancelAndJoin()
        }
    }

    @Test
    fun `job 이 invokeOnCompletion 이 호출되기 전에 완료되었으면, 핸들러는 즉시 호출된다`() {
        runBlocking {
            val job = launch {
                delay(300)
                log("Finished")
            }

            log("1")
            delay(800)
            log("2")

            job.invokeOnCompletion { cause: Throwable? ->
                log("will always be printed")
                log("The exception was ${cause}")
            }

            delay(800)
            job.cancelAndJoin()
        }


    }

    @Test
    fun `취소는 중단점에서 일어나서, 중단점이 없으면 취소할 수 없다`() {
        runBlocking {
            val job = Job()

            launch(job){
                repeat(1_000) { i ->
                    Thread.sleep(200) // 복잡한 연산이나 파일 읽는 등의 작업
                    log("printing $i")
                }
            }

            delay(1000)
            log(1)
            job.cancelAndJoin()
            log("cancelled")
            delay(1000)
        }
    }

    @Test
    fun `yield 를 주기적으로 호출해서 취소할 수 있다`() {
        runBlocking {
            val job = Job()

            launch(job){
                repeat(1_000) { i ->
                    Thread.sleep(200)
                    yield() // 코루틴을 중단하고 즉시 재실행 => 중단점이 생김
                    log("Printing $i")
                }
            }

            log("1")
            delay(1000)
            log("2")
            job.cancelAndJoin()
            log("cancelled")
            delay(1000)
        }
    }

    @Test
    fun `잡의 상태를 추적해서 취소할 수 있다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                log("1")
                do {
                    Thread.sleep(200)
                    log("printing")
                } while(isActive)
            }

            log("2")
            delay(1100)
            job.cancelAndJoin()
            log("cancelled")
        }
    }

    @Test
    fun `잡이 active 상태가 아니면 CancellationException 을 던지는 함수를 사용해서 취소할 수 있다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                repeat(1000){ num ->
                    Thread.sleep(200)
                    ensureActive()
                    log("printing $num")
                }
            }

            delay(1100)
            job.cancelAndJoin()
            log("cancelled")
        }
    }
}