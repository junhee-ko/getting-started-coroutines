package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

class Chapter11 {

    @Test
    fun `coroutineScope 함수는 새로운 코루틴을 생성하지만 새로운 코루틴이 끝날 때까지 coroutineScope 를 호출한 코루틴을 중단한다`() {
        runBlocking {
            val a: Int = coroutineScope {
                log("first coroutineScope")
                delay(10000)
                10
            }
            log("a is calculated")

            val b = coroutineScope {
                log("second coroutineScope")
                delay(1000)
                20
            }
            log("b is calculated")
        }
    }

    @Test
    fun `coroutineScope 는 모든 자식이 끝날 때 까지 종료되지 않는다`() {
        runBlocking(CoroutineName("CoroutineName-jko")) {

            log("before coroutineScope")

            coroutineScope {
                log("before first launch")

                launch {
                    log("first launch")
                    delay(1000)
                    val name = coroutineContext[CoroutineName]?.name
                    log("Finished task 1, CoroutineName: ${name}")
                }
                launch {
                    log("second launch")
                    delay(2000)
                    val name = coroutineContext[CoroutineName]?.name
                    log("Finished task 2, CoroutineName: ${name}")
                }

                log("after second launch")
            }

            log("after coroutineScope")
        }
    }

    @Test
    fun `coroutineScope 는 부모가 취소되면 아직 끝나지 않은 자식 코루틴이 전부 취소된다`() {

        runBlocking {

            log("runBlocking > before first launch")

            val job: Job = launch(CoroutineName("CoroutineName-jko")) {

                coroutineScope {
                    log("coroutineScope > before first launch")

                    launch {
                        log("coroutineScope > starts first launch")
                        delay(1000)
                        val name = coroutineContext[CoroutineName]?.name
                        log("Finished task 1, name: $name")
                    }

                    launch {
                        log("coroutineScope > starts second launch")
                        delay(2000)
                        val name = coroutineContext[CoroutineName]?.name
                        log("Finished task 2, name: $name")
                    }

                    log("coroutineScope > after second launch")
                }
            }

            log("runBlocking > after first launch")

            delay(1500)
            log("runBlocking > after delay")
            job.cancel()
            log("runBlocking > after cancel")
        }
    }

    @Test
    fun `withContext 함수는 기존 스코프와 컨텍스트가 다른 코루티 스코프를 설정하기 위해 사용된다`() {
        runBlocking(CoroutineName("CoroutineName - jko")) {

            log("before")

            withContext(CoroutineName("CoroutineName - child 1")) {
                log("start child 1")
                delay(1000)
                log("end child 1")
            }

            withContext(CoroutineName("CoroutineName - child 2")) {
                log("start child 2")
                delay(1000)
                log("end child 2")
            }

            log("after")
        }
    }

    @Test
    fun `supervisorScope 함수는 Job 을 SupervisorJob 으로 오버라이딩하기 때문에, 자식 코루틴이 예외를 던져도 취소되지 않는다`() {
        runBlocking {
            log("runBlocking > before")

            supervisorScope {
                log("supervisorScope > before first launch")

                launch {
                    log("supervisorScope > start first launch")
                    delay(1000)
                    throw Error()
                }

                launch {
                    log("supervisorScope > start second launch")
                    delay(2000)
                    log("supervisorScope > end second launch")
                }

                log("supervisorScope > after second launch")
            }

            log("runBlocking > after")
        }
    }

    @Test
    fun `supervisorScope 는 서로 독립적인 작업을 시작하는 함수에서 주로 사용된다`() {
        runBlocking {

            log("runBlocking > before supervisorScope")

            supervisorScope {
                repeat(10) {
                    launch {
                        log("Start launch $it")
                        throw Error()
                    }
                }
            }

            log("runBlocking > after supervisorScope")
        }
    }

    @Test
    fun `async 를 사용한다면, await 를 호출하고 async 코루틴이 예외로 끝나게 되면 await 는 예외를 다시 던진다, 그래서 발생하는 예외를 전부 처리하려면 try-catch 블록으로 모두 래핑해야한다`() {
        runBlocking {

            log("runBlocking > before supervisorScope")

            supervisorScope {
                log("supervisorScope > before repeat")

                repeat(10) {
                    val res: Deferred<Int> = async {
                        log("async > $it")
                        if (it % 2 == 0) {
                            throw Error()
                        }
                        it
                    }

                    try {
                        res.await()
                    } catch (e: Throwable) {
                        log(e)
                    }
                }

                log("supervisorScope > after repeat")
            }

            log("runBlocking > after supervisorScope")
        }
    }

    @Test
    fun `withTimeout 은 인자로 들어온 람다식을 실행할 때 시간 제한이 있다, 너무 오래 걸리면 람다식은 취소되고 TimeoutCancellationException 을 던진다`() {
        runBlocking {

            try {
                withTimeout(1500) {
                    delay(1000)
                    log("Still ing")
                    delay(1000)
                    log("DONE")
                }
            } catch (e: TimeoutCancellationException) {
                log("canceled, $e")
            }
        }
    }

    // 예외가 1 의 launch 에서 잡힌다.
    // 1에서 시작된 코루틴과 2의 launch 로 시작된 자식 코루틴 또한 취소된다.
    // 3에서 시작된 launch 에는 아무런 영향이 없다.
    @Test
    fun `코루틴 빌더 내부에서 TimeoutCancellationException 을 던지면 해당 코루틴만 취소가 되고 부모에게는 영향을 주지 않는다`() {
        runBlocking {
            launch {        // 1
                launch {    // 2
                    delay(2000)
                    log("not printed")
                }
                withTimeout(1000) {
                    delay(1500)
                }
            }
            launch {        // 3
                delay(2000)
                log("DONE")
            }
        }
    }

    @Test
    fun `withTimeoutOrNull 은 예외를 던지지 않고, 타임아웃을 초과하면 람다식이 취소되고 null 이 반환된다`() {
        runBlocking {

            val res: Int? = withTimeoutOrNull(5000) {
                delay(6000)
                1
            }
            log("res: $res")
        }
    }

    @Test
    fun `서로 다른 코루틴 스코프 함수의 두 기능이 모두 필요하면, 코루틴 스코프 함수에서 다른 기능을 가지는 코루틴 스코프 함수를 호출하면 된다`() {
        runBlocking {

            withContext(Dispatchers.Default) {
                withTimeout(1000) {

                }
            }
        }
    }

    // bad
    // coroutineScope 는 launch 로 시작된 코루틴이 끝나길 기다린다.
    // 1. coroutineScope 가 별도의 함수 내에서 동작한다면, launch 의 추가적인 연산이 함수의 목적과 관련된 유의미한 작업이 아니다.
    // 2. launch 가 취소 된다면, 전체 과정이 취소가 된다. 추가적인 연산이 취소 된다고 해서, 전체 과정이 취소되어서는 안된다.
    @Test
    fun `작업을 수행하는 중, 추가적인 연산을 수행하기 위해 동일한 스코프에서 launch 를 호출하는 경우`() {
        runBlocking {

            coroutineScope {
                val name = async {
                    log("coroutineScope > async 1")
                    delay(1000)
                    "jko"
                }
                val age = async {
                    log("coroutineScope > async 2")
                    delay(500)
                    20
                }

                log("name: ${name.await()}, age: ${age.await()}")

                launch {
                    log("coroutineScope > launch")
                }
            }
        }
    }

    @Test
    fun `핵심 동작에 영향을 주지 않는 추가적인 연산이 있으면, 다른 스코프에서 시작하면 된다`() {
        runBlocking {

            coroutineScope {
                val name = async {
                    log("coroutineScope > async 1")
                    delay(1000)
                    "jko"
                }
                val age = async {
                    log("coroutineScope > async 2")
                    delay(500)
                    20
                }

                log("name: ${name.await()}, age: ${age.await()}")

                CoroutineScope(SupervisorJob()).launch {
                    log("coroutineScope > launch")
                }

            }
        }
    }
}
