package fastcampus

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class StructuredConcurrency {

    private suspend fun doOneTwoThree() {
        // 부모 코루틴은 자식이 끝날 때 까지 기다린다
        // 즉, 아래 세 개의 launch 가 끝날 때 까지 기다림
        coroutineScope { // this: 코루틴
            launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch1: ${Thread.currentThread()}")
                delay(200L) // suspension point
                println("3!")
            }

            launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch2: ${Thread.currentThread()}")
                delay(2000L)
                println("1!")
            }

            launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch3: ${Thread.currentThread()}")
                delay(3000L)
                println("2!")
            }

            println("4!")
        }
    }

    @Test
    fun `코루틴 빌더를 suspend 함수 안에서 호출`() {
        runBlocking { // this: 코루틴. Receiver. 수신객체
            doOneTwoThree() // suspension point
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }

    private suspend fun doOneTwoThreeWithJob() {
        coroutineScope { // this: 코루틴
            val job: Job = launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch1: ${Thread.currentThread()}")
                delay(5000L) // suspension point
                println("3!")
            }
            job.join() // suspension point

            launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch2: ${Thread.currentThread()}")
                delay(2000L)
                println("1!")
            }

            launch { // this: 자식 코루틴. Receiver. 수신객체
                println("launch3: ${Thread.currentThread()}")
                delay(3000L)
                println("2!")
            }

            println("4!")
        }
    }

    @Test
    fun `코루틴 빌더인 launch 는 Job 객체를 반환해서 종료될 때 까지 기다릴 수 있다`() {
        runBlocking {
            doOneTwoThreeWithJob() // suspension point
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }

    private suspend fun doOneTwoThree_lightWeight() {
        coroutineScope {
            val job: Job = launch {
                println("launch1: ${Thread.currentThread()}")
                delay(5000L)
                println("3!")
            }
            job.join() // suspension point

            launch {
                println("launch2: ${Thread.currentThread()}")
                delay(2000L)
                println("1!")
            }

            repeat(10_0000) {
                launch {
                    println("launch3: ${Thread.currentThread()}")
                }
            }

            println("4!")
        }
    }

    // 코틀린은 협력적으로 동작해서, 여러 코틀린을 만드는 것이 큰 비용이 들지 않는다
    // 10 만개의 간단한 일을 하는 코틀린도 부담이 없다
    @Test
    fun `가벼운 코틀린`() {
        runBlocking {
            doOneTwoThree_lightWeight()
            println("runBlocking: ${Thread.currentThread()}")
            println("5!")
        }
    }
}