package fastcampus

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class `SuspendFunction_1-4` {

    private suspend fun getRandom1(): Int {
        delay(1000L)

        return Random.nextInt(0, 500)
    }

    private suspend fun getRandom2(): Int {
        delay(1000L)

        return Random.nextInt(0, 500)
    }

    @Test
    fun `suspend function 들의 순차적 수행`() {
        runBlocking {
            val elapsedTimes = measureTimeMillis {
                val value01 = getRandom1() // getRandom1 이 호출된 뒤에
                val value02 = getRandom2() // getRandom2 가 호출됨

                println("${value01} + ${value02} = ${value01 + value02}")
            }
            println(elapsedTimes)
        }
    }

    // async 도 launch 와 마찬가지로 코루틴 빌더
    // async 는 launch 와 비슷하지만, async 는 수행 결과를 받을 수 있다
    @Test
    fun `async 를 이용해서 동시 수행한다`() {
        runBlocking {
            val elapsedTimes = measureTimeMillis {
                val value01 = async {// this: 코루틴
                    getRandom1()
                }
                val value02 = async {// this: 코루틴
                    getRandom2()
                }

                // await: job.join() 역할을 하면서 결과도 가져옴
                // await 호출 지점: suspension point
                println("${value01.await()} + ${value02.await()} = ${value01.await() + value02.await()}")
            }
            println(elapsedTimes)
        }
    }

    // async 를 사용하면, 코드 블록이 수행을 예약한다
    // "start = CoroutineStart.LAZY" 를 붙이면 코루틴이 만들어지지만, 수행 예약이 되지는 않음
    @Test
    fun `async 를 게으르게 사용하기`() {
        runBlocking {
            val elapsedTimes = measureTimeMillis {
                val value01 = async(start = CoroutineStart.LAZY) {// this: 코루틴
                    getRandom1()
                }
                val value02 = async(start = CoroutineStart.LAZY) {// this: 코루틴
                    getRandom2()
                }

                value01.start() // 수행 예약이 되어서 큐에 들어감
                value02.start() // 수행 예약이 되어서 큐에 들어감

                // await: job.join() 역할을 하면서 결과도 가져옴
                // await 호출 지점: suspension point
                println("${value01.await()} + ${value02.await()} = ${value01.await() + value02.await()}")
            }
            println(elapsedTimes)
        }
    }

    private suspend fun getRandom1WithTryFinally(): Int {
        try {
            delay(1000L)
            return Random.nextInt(0, 500)
        } finally {
            println("getRandom1WithTryFinally is canceled")
        }

    }

    private suspend fun getRandom2WithThrow(): Int {
        delay(1000L)

        throw IllegalStateException()
    }

    private suspend fun doSomething() = coroutineScope { // 부모 코루틴 <-- 문제 발생했으니까 캔슬하라고 알려줌
        val value01 = async { // 자식 코루틴 <-- 문제 발생했으니까 캔슬하라고 알려줌
            getRandom1WithTryFinally()
        }
        val value02 = async { // 자식 코루틴 <- 문제 발생
            getRandom2WithThrow()
        }

        try {
            println("${value01.await()} + ${value02.await()} = ${value01.await() + value02.await()}")
        } finally {
            println("doSomething is canceled")
        }
    }

    // 다른 형제가 문제 생기면, 나머지 형제도 캔슬. 부모도 캔슬
    @Test
    fun `예외가 발생하면 위쪽 코루틴 스코프와 아래쪽 코루틴 스코프가 취소된다`() {
        runBlocking {
            try {
                doSomething()
            } catch (e: IllegalStateException) {
                println("doSomething failed: ${e}")
            }
        }
    }
}