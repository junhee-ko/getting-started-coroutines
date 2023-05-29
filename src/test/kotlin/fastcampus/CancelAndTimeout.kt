package fastcampus

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class CancelAndTimeout {

    private suspend fun doOneTwoThree() {
        coroutineScope {
            val job1: Job = launch {
                println("launch1: ${Thread.currentThread()}")
                delay(1000L)
                println("3!")
            }

            val job2 = launch {
                println("launch2: ${Thread.currentThread()}")
                println("1!")
            }

            val job3 = launch {
                println("launch3: ${Thread.currentThread()}")
                delay(500L)
                println("2!")
            }

            delay(800L)
            job1.cancel()
            job2.cancel()
            job3.cancel()
            println("4!")
        }
    }

    @Test
    fun `Job 에 대해 cancel 메서드를 호출해서 취소할 수 있다`() {
        runBlocking {
            doOneTwoThree()
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }

    suspend fun doCount() = coroutineScope {
        val job1 = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while (i <= 10) {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job1.cancel() // 취소가 되지 않음
        println("doCount DONE!") // job1 이 취소가 되든 종료가 되든 다 끝난 이후에 출력이 되어야하는데, 중간에 됨
    }

    @Test
    fun `취소 불가능한 Job`() {
        runBlocking {
            doCount()
        }
    }

    suspend fun doCountWithJoin() = coroutineScope {
        val job1 = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while (i <= 10) {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job1.cancel()
        job1.join() // cancel 을 호출한 뒤에 취소 안되었을 수도 있으니 기다림. 하지만 여기서 cancel 은 실제로 이뤄나지 않음
        println("doCount DONE!")
    }

    @Test
    fun `job1 다 끝난 이후에 doCount DONE 출력이 되도록 수정한다`() {
        runBlocking {
            doCountWithJoin()
        }
    }

    suspend fun doCountWithCancelAndJoin() = coroutineScope {
        val job1 = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while (i <= 10) {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job1.cancelAndJoin()
        println("doCount DONE!")
    }

    @Test
    fun `cancel을 하고 join 을 하는 일은 빈버하다`() {
        runBlocking {
            doCountWithCancelAndJoin()
        }
    }

    suspend fun doCountWithIsActive() = coroutineScope {
        val job1 = launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            // 활성화된 상태에서만 아래 코드를 돌림
            while (i <= 10 && isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }

        delay(200L)
        job1.cancelAndJoin()
        println("doCount DONE!")
    }

    @Test
    fun `isActive 를 통해서 cancel 가능한 코드를 만들 수 있다`() {
        runBlocking {
            doCountWithIsActive()
        }
    }

    private suspend fun doOneTwoThreeWithTryFinally() = coroutineScope {
        val job1 = launch {
            try {
                println("launch1: ${Thread.currentThread()}")
                delay(1000L)
                println("3!")
            } finally {
                println("job1 is finishing")
                // 파일을 닫는 코드
            }
        }

        val job2 = launch {
            try {
                println("launch2: ${Thread.currentThread()}")
                delay(1000L)
                println("1!")
            } finally {
                println("job2 is finishing")
                // 소켓을 닫는 코드
            }
        }

        val job3 = launch {
            try {
                println("launch3: ${Thread.currentThread()}")
                delay(1000L)
                println("2!")
            } finally {
                println("job3 is finishing")
                // 소켓을 닫는 코드
            }
        }

        delay(800L)
        job1.cancel()
        job2.cancel()
        job3.cancel()
        println("4!")
    }

    @Test
    fun `launch 에서 자원을 할당한 경우, try catch finally`() {
        runBlocking {
            doOneTwoThreeWithTryFinally()
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }

    private suspend fun doOneTwoThreeWithContext() = coroutineScope {
        val job1 = launch {
            withContext(NonCancellable) {
                println("launch1: ${Thread.currentThread()}")
                delay(1000L)
                println("3!")
            }
            delay(1000L)
            println("job1: end")
        }

        val job2 = launch {
            withContext(NonCancellable) {
                println("launch2: ${Thread.currentThread()}")
                delay(1000L)
                println("1!")
            }
            delay(1000L)
            println("job2: end")
        }

        val job3 = launch {
            withContext(NonCancellable) {
                println("launch3: ${Thread.currentThread()}")
                delay(1000L)
                println("2!")
            }
            delay(1000L)
            println("job3: end")
        }

        delay(800L)
        job1.cancel()
        job2.cancel()
        job3.cancel()
        println("4!")
    }

    @Test
    fun `취소가 불가능해야하는 코드`() {
        runBlocking {
            doOneTwoThreeWithContext()
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }

    suspend fun doCountWithTimeout() = coroutineScope {
        launch(Dispatchers.Default) {
            var i = 1
            var nextTime = System.currentTimeMillis() + 100L

            while (i <= 10 && isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime >= nextTime) {
                    println(i)
                    nextTime = currentTime + 100L
                    i++
                }
            }
        }
    }

    @Test
    fun `일정 시간이 지난 후에 종료하고 싶으면 timeout 을 사용한다`() {
        runBlocking {
            withTimeout(500L) { // TimeoutCancellationException 발생
                doCountWithTimeout()
                println("runBlocking: ${Thread.currentThread()}")
                println("5!")
            }
        }
    }

    @Test
    fun `withTimeoutOrNull 을 이용해 타임 아웃할 때 null 을 반한한다`() {
        runBlocking {
            val result: Boolean = withTimeoutOrNull(500L) {
                doCountWithTimeout()
                println("runBlocking: ${Thread.currentThread()}")
                println("5!")
                true
            } ?: false
            result

            println(result)
        }
    }
}