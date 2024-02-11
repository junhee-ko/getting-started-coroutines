package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

class Chapter10 {

    @Test
    fun `코루틴이 예외를 받았을 때 자기 자신을 취소하고 예외를 부모로 전파한다, 부모는 자기 자신과 자식들 모두 취소하고 예외를 부모에서 전파한다`() {
        runBlocking {// 1

            launch {// 2-1

                launch {// 3-1
                    delay(1000)
                    throw Error()
                }

                launch {// 3-2
                    delay(2000)
                    log("not be printed")
                }

                launch {// 3-3
                    delay(500)
                    log("printed")
                }
            }

            launch {// 2-2
                delay(2000)
                log("not be printed")
            }
        }
    }

    @Test
    fun `코루틴 간의 상호작용은 Job 을 통해서 일어나기 때문에 코루틴 빌더 내부에서 새로운 코루틴 빌더를 try-catch 문을 통해 래핑하는 것은 무시된다`() {
        runBlocking {
            try {
                launch {
                    delay(1000)
                    throw Error()
                }
            } catch (e: Throwable) {
                log("will not be printed")
            }

            launch {
                delay(2000)
                log("will not be printed")
            }
        }
    }

    @Test
    fun `코루틴 종료를 멈추는 가장 중요한 방법은 SupervisorJob 을 사용하는 것이다, 자식에서 발생한 모든 예외를 무시할 수 있다`() {
        runBlocking {
            val scope = CoroutineScope(SupervisorJob())

            scope.launch {
                delay(1000)
                throw Error()
            }

            scope.launch {
                delay(2000)
                log("printed")
            }

            delay(3000)
        }
    }

    @Test
    fun `흔한 실수 중 하나는, SupervisorJob 을 부모 코루틴의 인자로 사용하는 것이다, 이 경우 SupervisorJob 은 단 하나의 자식만 갖는다`() {
        runBlocking {
            val supervisorJob = SupervisorJob()

            launch(supervisorJob) {

                launch {
                    delay(1000)
                    throw Error()
                }

                launch {
                    delay(2000)
                    log("not printed")
                }
            }

            delay(3000)
        }
    }

    @Test
    fun `같은 잡을 다수의 코루틴에서 컨텍스트로 사용하는 것이 더 나은 방법이다`() {
        runBlocking {
            val supervisorJob = SupervisorJob()

            launch(supervisorJob) {
                delay(1000)
                throw Error()
            }

            launch(supervisorJob) {
                delay(2000)
                log("printed")
            }

            log("1")
            supervisorJob.children.forEach { it.join() }
            log("2")
        }
    }

    // supervisorScope 를 사용하는 일반적인 방법: 서로 무관한 다수의 작업을 스코프 내에서 실행할 때
    @Test
    fun `예외 전파를 막는 또 다른 방법은 코루틴 빌더를 supervisorScope 으로 래핑하는 것이다`() {
        runBlocking {

            log(1)
            supervisorScope {
                launch {
                    delay(1000)
                    throw Error()
                }

                launch {
                    delay(2000)
                    log("printed")
                }
            }
            log(2)
            delay(1000L)
            log(3)
            log("DONE")
        }
    }

    @Test
    fun `supervisorScope 가 사용되어서, 또 다른 async 는 중단되지 않고 실행된다`() {
        runBlocking {

            supervisorScope {

                val str1 = async {
                    delay(1000)
                    throw Error()
                }

                val str2 = async {
                    delay(1000)
                    "2"
                }

                try {
                    str1.await()
                } catch (e: Throwable) {
                    log(e)
                }

                log(str2.await())
            }

        }
    }

    // 3의 예외는, 1에서 시작된 launch 에서 잡힌다.
    // 1에서 시작된 코루틴은 자기 자신을 취소하고 2에서 정의된 빌더로 만들어진 자식 코루틴 또한 취소한다.
    // 4에서 시작된 코루틴은 전혀 영향 받지 않는다.
    @Test
    fun `CancellationException 은 부모까지 전파되지 않는다`() {
        runBlocking {

            coroutineScope {

                launch {    // 1
                    launch {    // 2
                        delay(2000)
                        log("not printed")
                    }
                    throw CancellationException() // 3
                }

                launch {    // 4
                    delay(2000)
                    log("printed")
                }
            }
        }
    }

    @Test
    fun `코루틴 예외 헨들러 - 예외를 다룰 때 예외를 처리하는 기본 행동을 정의하는 것이 유용할 때가 있다`() {
        runBlocking {

            val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
                log("Caught ${throwable}")
            }

            val scope = CoroutineScope(SupervisorJob() + coroutineExceptionHandler)

            scope.launch {
                log(2)
                delay(1000)
                throw Error()
            }

            scope.launch {
                log(3)
                delay(2000)
                log("printed")
            }

            log(1)
            delay(3000)
        }
    }
}