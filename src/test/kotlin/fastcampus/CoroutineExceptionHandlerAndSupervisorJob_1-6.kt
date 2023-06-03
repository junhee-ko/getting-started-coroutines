package fastcampus

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.lang.ArithmeticException
import kotlin.random.Random

class `CoroutineExceptionHandlerAndSupervisorJob_1-6` {

    private suspend fun printRandom() {
        delay(500L)
        println(Random.nextInt(0, 500))
    }

    // 어디에도 속하지 않지만 원래부터 존재하는 전역 GlobalScope
    // GlobalScope 는 어떤 계층에도 속하지 않아 관리가 어렵다
    // 일반적으로는 사용하지 않음
    @Test
    fun GlobalScope() {
        val job: Job = GlobalScope.launch(Dispatchers.IO) {
            launch { printRandom() }
        }

        Thread.sleep(1000L)
    }

    // CoroutineScope 이 GlobalScope 보다 권장
    // CoroutineScope 는 인자로 CoroutineContext 를 받음
    @Test
    fun CoroutineScope() {
        val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName("jko"))
        coroutineScope.launch(Dispatchers.IO) {
            printRandom()
        }

        Thread.sleep(1000L)
    }

    private suspend fun printRandom01() {
        delay(1000L)
        println(Random.nextInt(0, 500))
    }

    private suspend fun printRandom02() {
        delay(500L)
        throw ArithmeticException()
    }

    val ceh = CoroutineExceptionHandler { _, exception ->
        println("Something happened: $exception")
    }

    @Test
    fun CroutineExceptionHandler() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.IO)
            val job = scope.launch(context = ceh) {
                launch { printRandom01() }
                launch { printRandom02() }
            }
            job.join()
        }
    }

    private suspend fun getRandom01(): Int {
        delay(1000L)

        return Random.nextInt(0, 500)
    }

    private suspend fun getRandom02(): Int {
        delay(500L)

        throw ArithmeticException()
    }

    // runBlocking 은 자식이 종료되면 항상 종료됨
    // CEH 를 호출하지 않음
    @Test
    fun `runBlocking 에서는 CHE 를 사용할 수 없다`() {
        runBlocking { // 1. 최상단 코루틴
            val job = launch(ceh) { // 2
                val value01: Deferred<Int> = async { getRandom01() } // 3
                val value02: Deferred<Int> = async { getRandom02() } // 4
                println(value01.await())
                println(value02.await())
            }
            job.join()
        }
    }

    // 일반적인 Job 은 예외가 발생하면 캔슬을 위/아래 모두 한다.
    // 즉, 자식(A) 이 문제가 생기면 부모도 취소하고 A 의 자식도 캔슬한다.
    @Test
    fun `슈퍼 바이저 잡은 예외에 의한 취소를 아래쪽으로 내려가게 한다`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + ceh)

            val job01: Job = scope.launch { printRandom01() }
            val job02 = scope.launch { printRandom02() } // job2에서 발생한 문제는 job2의 자식에게만 영향을 줌


            // == job1.join + job2.join
            joinAll(job01, job02) // 북수개의 Job에 대해 join를 수행하여 완전히 종료될 때까지 기다린다.
        }
    }

    suspend fun supervisoredFunc(){
        supervisorScope {
            launch { printRandom01() }
            launch(ceh) { printRandom02() } // 자식에서 꼭 예외를 헨들링해야함. 자식의 실패가 부모에게 전달되지 않기 때문에
        }
    }

    @Test
    fun `SupervisorScope = 코루틴 스코프 + 슈퍼바이저잡`() {
        runBlocking{
            val scope = CoroutineScope(Dispatchers.IO)
            val job = scope.launch {
                supervisoredFunc()
            }
            job.join()
        }
    }
}