package fastcampus

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class `CoroutineContextAndDispatcher_1-5` {

    @Test
    fun `dispatchers 여러 종류`() {
        runBlocking {
            launch {
                println("부모의 컨텍스트 / ${Thread.currentThread().name}")
            }

            // 코어 수에 비례하는 스레프 풀에서 수행
            // Dispatchers.IO 와 같은 워커 스레드를 사용함, but 정책이 다름
            // Default 는 복잡한 연산을 시키기 위한 것
            // 복잡한 연산은 여러 스레드를 만들면 효율적이지 않음
            // 코어는 한 가지 일밖에 못하는데, 여러 스레드로 나누어서 처리해야함
            launch(Dispatchers.Default) {
                println("Default / ${Thread.currentThread().name}")
            }

            // 코어 수 보다 훨씬 많은 스레드를 가지는 스레드 풀
            // IO 작업은 CPU 를 덜 소모하기 때문
            // 파일을 읽을 때, 네트워크로 뭘 가져올 때 CPU 를 소모하지 않음
            // 그래서 IO 작업 할 때 스레드 많아도 비용이 크지 않음
            launch(Dispatchers.IO) {
                println("IO / ${Thread.currentThread().name}")
            }

            // 어디에도 속하지 않음 == 앞으로도 어디에서 수행될지 모름, 즉 Dispatchers.IO 로 수행될지, newSingleThread로 수행될지 모름..
            // 그래서 일반적으로 많이 사용하지 않음
            // 지금 시점에는 부모의 스레드에서 수행될 것
            launch(Dispatchers.Unconfined) {
                println("Unconfined / ${Thread.currentThread().name}")
                delay(100L)
                println("Unconfined / ${Thread.currentThread().name}") // 위 프린트의 결과와 다르게 다른 스레드에서 처리 가능
            }

            // 항상 새로운 스레드를 만듦
            // IO 나 Default 는 항상 스레드 풀에 있는 스레드를 사용함
            // 그래서, 다른 스레드가 많이 쓰이고 있으면, 스레드를 할당받지 못할 수도 있음
            // 무조건 받아야하면, newSingleThreadContext 를 사용할 수 있음
            launch(newSingleThreadContext("jko")) {
                println("newSingleThreadContext / ${Thread.currentThread().name}")
            }
        }
    }

    @Test
    fun `launch 외에 async, withContext 등의 코루틴 빌더에서도 디스패처를 사용할 수 있다`() {
        runBlocking {
            async {
                println("부모의 컨텍스트 / ${Thread.currentThread().name}")
            }

            // 코어 수에 비례하는 스레프 풀에서 수행
            // Dispatchers.IO 와 같은 워커 스레드를 사용함, but 정책이 다름
            // Default 는 복잡한 연산을 시키기 위한 것
            // 복잡한 연산은 여러 스레드를 만들면 효율적이지 않음
            // 코어는 한 가지 일밖에 못하는데, 여러 스레드로 나누어서 처리해야함
            withContext(Dispatchers.Default) {
                println("Default / ${Thread.currentThread().name}")
            }

            // 코어 수 보다 훨씬 많은 스레드를 가지는 스레드 풀
            // IO 작업은 CPU 를 덜 소모하기 때문
            // 파일을 읽을 때, 네트워크로 뭘 가져올 때 CPU 를 소모하지 않음
            // 그래서 IO 작업 할 때 스레드 많아도 비용이 크지 않음
            async(Dispatchers.IO) {
                println("IO / ${Thread.currentThread().name}")
            }

            // 어디에도 속하지 않음. 앞으로도 어디에서 수행될지 모름, 즉 Dispatchers.IO 로 수행될지, newSingleThread로 수행될지 모름..
            // 그래서 일반적으로 많이 사용하지 않음
            // 지금 시점에는 부모의 스레드에서 수행될 것
            async(Dispatchers.Unconfined) {
                println("Unconfined / ${Thread.currentThread().name}")
            }

            // 항상 새로운 스레드를 만듦
            // IO 나 Default 는 항상 스레드 풀에 있는 스레드를 사용함
            // 그래서, 다른 스레드가 많이 쓰이고 있으면, 스레드를 할당받지 못할 수도 있음
            // 무조건 받아야하면, newSingleThreadContext 를 사용할 수 있음
            async(newSingleThreadContext("jko")) {
                println("newSingleThreadContext / ${Thread.currentThread().name}")
            }
        }
    }

    // 중단점 이후에 어느 디스패처에서 수행될지 예측이 힘듦
    // 가능하면 확실한 디스페처를 사용하는 것이 좋음
    @Test
    fun `Confined 는 처음에는 부모의 스레드에서 수행하지만, 한번 중단점이 오면 바뀐다`() {
        runBlocking {
            async(Dispatchers.Unconfined) {
                println("Unconfined / ${Thread.currentThread().name}")
                delay(100L)
                println("Unconfined / ${Thread.currentThread().name}")
                delay(100L)
                println("Unconfined / ${Thread.currentThread().name}")
            }
        }
    }

    @Test
    fun `부모가 있는 Job 과 없는 Job`() {
        runBlocking {
            val job = launch {

                // Job 을 만들면 더이상 부모 자식 관계가 아님
                // 부모 형제 관계가 아님.
                // 부모는 얘를 자식으로 보지 않고, 얘가 끝날 때까지 이제 기다리지 않음
                // 에러가 나도 부모/형제가 무시함
                launch(Job()) {
                    println(coroutineContext[Job])
                    println("launch1: ${Thread.currentThread().name}")
                    delay(1000L)
                    println("3!")
                }

                launch {
                    println(coroutineContext[Job])
                    println("launch2: ${Thread.currentThread().name}")
                    delay(1000L)
                    println("1!")
                }
            }

            delay(500L)
            job.cancelAndJoin() // 여기서도 Job 만든 launch 블럭 코드를 기다리지 않음. 밑에서 딜레이를 주지 않으면 바로 종료됨
            delay(1000L)
        }
    }

    @Test
    fun `부모는 자식이 끝날 때 까지 기다린다`() {
        runBlocking {
            val elapsed = measureTimeMillis {
                val job = launch { // 부모
                    launch { // 자식 1
                        println("launch1: ${Thread.currentThread().name}")
                        delay(5000L)
                    }

                    launch { // 자식 2
                        println("launch2: ${Thread.currentThread().name}")
                        delay(10L)
                    }
                }
                job.join()
            }

            println(elapsed)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `코루틴 엘리먼트 결합`() {
        runBlocking {
            // A (부모 컨텍스트)
            launch {

                // Dispatchers.IO -> B
                // CoroutineName("launch1") -> C
                // 여기서는 그럼 컨텍스트가 A+B+C 가 됨
                launch (Dispatchers.IO + CoroutineName("launch1")){
                    println("launch1: ${Thread.currentThread().name}")
                    println(coroutineContext[CoroutineDispatcher])
                    println(coroutineContext[CoroutineName])
                    delay(5000L)
                }

                launch (Dispatchers.Default + CoroutineName("launch2")){
                    println("launch2: ${Thread.currentThread().name}")
                    println(coroutineContext[CoroutineDispatcher])
                    println(coroutineContext[CoroutineName])
                    delay(10L)
                }
            }
        }
    }
}