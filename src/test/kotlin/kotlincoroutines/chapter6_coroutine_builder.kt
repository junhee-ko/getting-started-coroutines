package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

class Chapter6 {

    @Test
    fun launch() {
        GlobalScope.launch {
            log("1")
            delay(1000L)
        }
        GlobalScope.launch {
            log("2")
            delay(1000L)
        }
        GlobalScope.launch {
            log("3")
            delay(1000L)
        }

        log("4")
        Thread.sleep(3000)
    }

    @Test
    fun `runBlocking 은 코루틴이 중단되면 시작한 스레드를 중단시킨다`() {
        runBlocking {
            delay(1000L)
            log("1")
        }
        runBlocking {
            delay(1000L)
            log("2")
        }
        runBlocking {
            delay(1000L)
            log("3")
        }
        log("4")
    }

    @Test
    fun `runBlocking 안에서 delay 를 사용`() {
        runBlocking {
            GlobalScope.launch {
                delay(1000)
                log(1)
            }
            GlobalScope.launch {
                delay(1000)
                log(2)
            }
            GlobalScope.launch {
                delay(1000)
                log(3)
            }
            log(4)
            delay(2000)
        }
    }

    @Test
    fun `async 코루틴 빌더는 값을 생성한다`() {
        runBlocking {
            val deferred: Deferred<Int> = GlobalScope.async {
                delay(1000L)
                33
            }
            val result: Int = deferred.await()
            log(result.toString())
        }
    }

    @Test
    fun `값이 생성되기 전에 await 를 호출하면 값이 나올 때 까지 기다린다`() {
        runBlocking{
            val result1 = GlobalScope.async {
                log(1)
                delay(5000L)
                1
            }
            val result2 = GlobalScope.async {
                log(2)
                delay(9000L)
                2
            }
            val result3 = GlobalScope.async {
                log(3)
                delay(7000L)
                3
            }

            log(4)
            log(result1.await().toString())
            log(result2.await().toString())
            log(result3.await().toString())
        }
    }

    @Test
    fun `async 는 값을 생성할 때 사용되며, 값이 필요하지 않을 때는 launch 를 써야한다`() {
        runBlocking{
            GlobalScope.async {
                delay(1000)
                log(1)
            }
            log(2)
            delay(2000)
        }
    }

    // 구조화된 동시성
    // 자식은 부모로부터 컨텍스트를 상속받는다.
    // 부모는 자식이 작업을 마칠 때까지 기다린다.
    // 부모 코루틴이 취쇠되면 자식 코루틴도 취소된다.
    // 자식 코루틴에서 에러가 발생하면, 부모 코루틴도 에어로 소멸한다.
    @Test
    fun `launch 는 runBlocking 의 자식이 되므로, runBlocking 은 모든 자식이 작업을 마칠때 까지 중단된다`() {
        runBlocking {
            this.launch {
                log("first child")
                delay(1000L)
                log("1")
            }
            launch {
                log("second child")
                delay(2000L)
                log("2")
            }
            log("3")
        }
    }
}