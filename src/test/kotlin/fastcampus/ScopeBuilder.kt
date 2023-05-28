package fastcampus

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ScopeBuilder {

    @Test
    fun `코루틴 빌더의 수신 객체`() = runBlocking {
        println(this)
        println(Thread.currentThread())
        println("Hello")
    }

    @Test
    fun `코루틴 컨텍스트`() = runBlocking {
        println(this.coroutineContext)
        println(Thread.currentThread())
        println("Hello")
    }

    @Test
    fun `launch 코루틴 빌더`() = runBlocking {
        launch {
            println("launch: ${Thread.currentThread()}")
            println("World")
        }
        println("runBlocking: ${Thread.currentThread()}")
        println("Hello")
    }

    @Test
    fun delay() = runBlocking {
        launch {
            println("launch: ${Thread.currentThread()}")
            println("World")
        }
        println("runBlocking: ${Thread.currentThread().name}")
        delay(5000L)
        println("Hello")
    }

    @Test
    fun `thread sleep`() = runBlocking {
        launch {
            println("launch: ${Thread.currentThread()}")
            println("World")
        }
        println("runBlocking: ${Thread.currentThread()}")
        Thread.sleep(5000L)
        println("Hello")
    }

    @Test
    fun `한 번에 여러 launch1`() = runBlocking {
        launch {
            println("launch1: ${Thread.currentThread()}")
            delay(1000L) // suspension point
            println("3!")
        }

        launch {
            println("launch2: ${Thread.currentThread()}")
            println("1!")
        }

        println("runBlocking: ${Thread.currentThread()}")
        delay(500L) // suspension point
        println("2!")
    }

    @Test
    fun `한 번에 여러 launch2`() = runBlocking {
        launch {
            println("launch1: ${Thread.currentThread()}")
            delay(200L) // suspension point
            println("3!")
        }

        launch {
            println("launch2: ${Thread.currentThread()}")
            delay(1000L)
            println("1!")
        }

        println("runBlocking: ${Thread.currentThread()}")
        delay(500L) // suspension point
        println("2!")
    }

    @Test
    fun `runBlocking 은 그 속에 포함된 launch 가 다 끝나기 전까지 종료되지 않는다`() {
        // runBlocking 은계층적, 구조적인
        runBlocking {
            launch {
                println("launch1: ${Thread.currentThread()}")
                delay(200L) // suspension point
                println("3!")
            }

            launch {
                println("launch2: ${Thread.currentThread()}")
                delay(2000L)
                println("1!")
            }

            println("runBlocking: ${Thread.currentThread()}")
            delay(500L) // suspension point
            println("2!")
        }

        println("4!")
    }

    @Test
    fun `delay, launch 등의 함수들을 분리하고 싶으면 suspend 키워드를 붙이면 된다`() {
        runBlocking{
            launch {
                doThree()
            }
            launch {
                doOne()
            }

            doTwo()
        }
    }

    private suspend fun doThree() {
        println("launch1: ${Thread.currentThread()}")
        delay(3000L)
        println("3!")
    }

    private suspend fun doOne() {
        println("launch2: ${Thread.currentThread()}")
        println("1!")
    }

    private suspend fun doTwo() {
        println("runBlocking: ${Thread.currentThread()}")
        delay(5000L)
        println("2!")
    }
}