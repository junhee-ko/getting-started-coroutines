package learningcuncurrencyinkotlin

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import kotlin.system.measureTimeMillis

class Chapter4 {

    @Test
    fun `일시 중단 연산은 다른 일시 중단 연산에서만 호출 가능`() {
        //Suspend function 'greetDelayed' should be called only from a coroutine or another suspend function
        runBlocking {
            greetDelayed(1000)
        }
    }

    private suspend fun greetDelayed(delayMillis: Long) {
        delay(delayMillis)
        log("Hello, Wolrd !")
    }

    @Test
    fun `비동기 함수로 repository 작성`() {
        runBlocking {
            val profileServiceClient = ProfileServiceClient()
            val asyncFetchById: Deferred<Profile> = profileServiceClient.asyncFetchById(1L)
            val profile: Profile = asyncFetchById.await()
            log(profile.toString())
        }
    }

    data class Profile(
        val id: Long,
        val name: String,
        val age: Int
    )

    interface ProfileServiceRepository {
        fun asyncFetchByName(name: String): Deferred<Profile>
        fun asyncFetchById(id: Long): Deferred<Profile>
    }

    class ProfileServiceClient : ProfileServiceRepository {

        override fun asyncFetchByName(name: String): Deferred<Profile> {
            return GlobalScope.async {
                Profile(1, name, 28)
            }
        }

        override fun asyncFetchById(id: Long): Deferred<Profile> {
            return GlobalScope.async {
                Profile(id, "jko", 28)
            }
        }
    }

    @Test
    fun `일시 중단 함수로 리팩토링`() {
        runBlocking {
            val profileServiceClientV2 = ProfileServiceClientV2()
            val profile: Profile = profileServiceClientV2.fetchById(12L)
            println(profile.toString())
        }

    }

    interface ProfileServiceRepositoryV2 {
        suspend fun fetchByName(name: String): Profile
        suspend fun fetchById(id: Long): Profile
    }

    class ProfileServiceClientV2 : ProfileServiceRepositoryV2 {
        override suspend fun fetchByName(name: String): Profile {
            return Profile(1, name, 28)
        }

        override suspend fun fetchById(id: Long): Profile {
            return Profile(id, "jko", 28)
        }
    }

    @Test
    fun defaultDispatcher() {
        runBlocking {
            GlobalScope.launch(Dispatchers.Default) {
                log("1")
            }
            GlobalScope.launch {
                log("2")
            }
            delay(2000L)
        }
    }

    @Test
    fun `unconfined - 첫 번째 중단 지점에 도달할 때 까지 현재 스레드에 있는 코루틴을 실행하고, 일시 중지된 후에 일시 중단 연산에서 사용된 기존 스레드에서 다시 시작`() {
        runBlocking {
            log("before launch")

            GlobalScope.launch(Dispatchers.Unconfined) {
                log("Start")
                delay(500)
                log("Resume")
            }.join()

            log("after launch")
        }
    }

    @Test
    fun `항상 코루틴이 특정 스레드 안에서 실행됨을 보장`() {
        runBlocking {
            val dispatcher = newSingleThreadContext("jkoThread")

            GlobalScope.launch(dispatcher) {
                log("Start")
                delay(500)
                log("Resume")
            }.join()
        }
    }

    @Test
    fun `스레드 풀을 가지고 있으며 해당 풀에서 가용한 스레드에서 코루틴을 시작하고 재개`() {
        log("before runBlocking")

        runBlocking {
            val dispatcher = newFixedThreadPoolContext(4, "jkoThread")

            log("before launch")

            GlobalScope.launch(dispatcher) {
                log("Start")
                delay(500)
                log("Resume")
            }.join()

            log("after launch")
        }

        log("after runBlocking")
    }

    @Test
    fun `코루틴 컨텍스트의 또 다른 중요한 용도는 예외에 대한 동작 정의`() {
        runBlocking {
            val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
                log("error in ${coroutineContext}")
                log("message: ${throwable}")
            }

            GlobalScope.launch(coroutineExceptionHandler) {
                throw RuntimeException()
            }

            delay(500)
        }
    }

    @Test
    fun `코루틴의 실행이 취소되면, CancellationException`() {
        runBlocking {
            val time = measureTimeMillis {
                val job = launch {
                    try {
                        while (isActive) {
                            delay(500)
                            log("running")
                        }
                    } finally {
                        log("cancelled, will delay")
                        delay(5000) // 지연이 되지 않음. 취소 중인 코루틴은 일시 중단 될 수 없기 때문이다
                        log("completed delay")
                    }
                }

                delay(1200)
                job.cancelAndJoin()
            }

            log("Took ${time}")
        }
    }

    @Test
    fun `코루틴이 취소되는 동안 일시 중지가 필요하면, NonCancellable`() {
        runBlocking {
            val time = measureTimeMillis {
                val job = launch {
                    try {
                        while (isActive) {
                            delay(500)
                            log("running")
                        }
                    } finally {
                        withContext(NonCancellable) {
                            log("cancelled, will delay")
                            delay(5000)
                            log("completed delay")
                        }
                    }
                }

                delay(1200)
                job.cancelAndJoin()
            }

            log("Took ${time}")
        }
    }

    @Test
    fun `컨텍스트 조합 - 특정 스레드에서 실행하는 코루틴 + 해당 스레드를 위한 예외 처리`() {
        runBlocking {
            val dispatcher = newSingleThreadContext("jkoDispatcher")
            val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
                log("Error captured: ${coroutineContext}")
                log("Message: ${throwable}")
            }

            GlobalScope.launch(dispatcher + handler) {
                log("running")
                throw RuntimeException()
            }.join()
        }
    }

    @Test
    fun `결합된 컨텍스트에서 컨텍스트 요소 제거`() {
        runBlocking {
            val dispatcher = newSingleThreadContext("jkoDispatcher")
            val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
                log("Error captured: ${coroutineContext}")
                log("Message: ${throwable}")
            }

            val context = dispatcher + handler
            val tempContext = context.minusKey(dispatcher.key)

            GlobalScope.launch(tempContext) {
                log("running")
                throw RuntimeException()
            }.join()
        }
    }

    @Test
    fun `await 를 호출해서 name 이 준비될 때 까지 일시 중단`() {
        runBlocking {
            val dispatcher = newSingleThreadContext("jkoThread")
            val name = GlobalScope.async(dispatcher) {
                "jko"
            }.await()
            log(name)
        }
    }

    // 다른 스레드에서 작업을 설정해야할 필요가 있으면, 계속 진행하기 전에 해당 작업 끝날 때까지 항상 기다림
    @Test
    fun `join 이나 await 를 호출하지 않고 순차적으로 동작`() {
        runBlocking {
            val dispatcher = newSingleThreadContext("jkoThread")
            val name = withContext(dispatcher) {
                log("")
                "jko"
            }
            log(name)
        }
    }
}