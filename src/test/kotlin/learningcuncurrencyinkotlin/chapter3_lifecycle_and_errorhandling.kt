package learningcuncurrencyinkotlin

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class Chapter3 {

    @Test
    fun `launch 를 이용해서 job 생성`() {
        runBlocking {
            val job = GlobalScope.launch {

            }
        }
    }

    @Test
    fun `job factory function 를 이용해서 job 생성`() {
        runBlocking {
            val job = Job()
        }
    }

    @Test
    fun `job 내부에서 발생하는 예외는 잡을 생성한 곳까지 전파`() {
        runBlocking {
            GlobalScope.launch {
                throw RuntimeException("")
            }
            delay(500)
        }
    }

    @Test
    fun `job 생성할 때 자동으로 시작되지 않게 하려면, LAZY`() {
        runBlocking {
            GlobalScope.launch(start = CoroutineStart.LAZY) {
                throw RuntimeException("")
            }
            delay(500)
        }
    }

    @Test
    fun `생성 상태의 job 을 시작하는 방법 중 start 는 잡이 완료될 때까지 기다리지 않는다`() {
        val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
            delay(3000)
            log("after delay")
        }
        job.start()
    }

    @Test
    fun `생성 상태의 job 을 시작하는 방법 중 join 은 잡이 완료될 때까지 실행을 일시 중단한다`() {
        runBlocking {
            val job = GlobalScope.launch(start = CoroutineStart.LAZY) {
                delay(3000)
                log("after delay")
            }
            job.join()
        }
    }

    @Test
    fun `취소 요청을 받은 active job 은 canceling 상태로 들어간다`() {
        runBlocking {
            val job = GlobalScope.launch {
                delay(5000)
            }
            delay(2000)
            log(job.isActive.toString())
            log(job.isCompleted.toString())
            log(job.isCancelled.toString())
            job.cancel()
            log(job.isActive.toString())
            log(job.isCompleted.toString())
            log(job.isCancelled.toString()) // 상태가 cancelling, cancelled 이면 true
        }
    }

    @Test
    fun `취소 또는 처리되지 않은 예외로 인해 실행이 종료된 잡은 cancelled 상태 - coroutineExceptionHandler`() {
        runBlocking {
            val coroutineExceptionHandler = CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
                log("Job cancelled due to ${throwable}")
            }

            GlobalScope.launch(coroutineExceptionHandler) {
                throw RuntimeException()
            }

            delay(500)
        }
    }

    @Test
    fun `취소 또는 처리되지 않은 예외로 인해 실행이 종료된 잡은 cancelled 상태 - invokeOnCompletion`() {
        runBlocking {
            GlobalScope.launch {
                throw RuntimeException()
            }.invokeOnCompletion { cause -> log("Job cancelled due to ${cause}") }

            delay(500)
        }
    }

    @Test
    fun `deferred 를 만들려면 async`() {
        runBlocking {
            val job: Deferred<Unit> = GlobalScope.async {
                delay(100L)
            }
            job.await()
        }
    }

    @Test
    fun `job 과 달리 deferred 는 예외를 자동으로 전파히지 않는다`() {
        runBlocking {
            val job: Deferred<Unit> = GlobalScope.async {
                throw RuntimeException()
            }
            delay(200L)
        }
    }

    @Test
    fun `job 과 달리 deferred 는 예외를 자동으로 전파히지 않는다 - await 로 전파하기`() {
        runBlocking {
            val job: Deferred<Unit> = GlobalScope.async {
                throw RuntimeException()
            }
            job.await()
        }
    }

    @Test
    fun `job 과 달리 deferred 는 예외를 자동으로 전파히지 않는다 - await 로 전파하고 try-catch 로 처리`() {
        runBlocking {
            val job: Deferred<Unit> = GlobalScope.async {
                throw RuntimeException()
            }

            try {
                job.await()
            } catch (e: Exception) {
                log("catched, ${e}")
            }
        }
    }

    @Test
    fun `job 이 특정 상태로 도달하면, 이전 상태로 되돌릴 수 없다`() {
        runBlocking {
            val time = measureTimeMillis {
                val job = GlobalScope.launch {
                    delay(2000L)
                }
                job.join()

                // restart
                job.start()
                job.join()
            }

            log(time.toString())
        }
    }

    @Test
    fun name() {
        runBlocking {
            GlobalScope.launch {
                delay(1000L)
                log("1")
            }
            GlobalScope.launch {
                delay(1000L)
                log("2")
            }
            GlobalScope.launch {
                delay(1000L)
                log("3")
            }
            log("4")
            delay(3000)
        }
    }
}