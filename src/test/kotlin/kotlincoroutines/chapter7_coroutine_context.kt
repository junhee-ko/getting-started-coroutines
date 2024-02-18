package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class Chapter7 {

    @Test
    fun `CoroutineName 이나 Job 은 CoroutineContext 인터페이스를 구현한 CoroutineContext Element 를 구현한다`() {
        val coroutineName: CoroutineName = CoroutineName("A")
        val element: CoroutineContext.Element = coroutineName
        val context: CoroutineContext = element

        val job: Job = Job()
        val jobElement: CoroutineContext.Element = job
        val jobContext: CoroutineContext = jobElement
    }

    @Test
    fun `CoroutineContext 는 컬렉션과 비슷해서, get 을 이용해서 유일한 키를 가진 원소를 찾을 수 있다`() {
        val context: CoroutineContext = CoroutineName("jko")

        val coroutineName: CoroutineName? = context[CoroutineName]
        log(coroutineName)

        val job: Job? = context[Job]
        log(job)
    }

    @Test
    fun `다른 키를 가진 두 원소를 더하면 만들어진 컨텍스트는 두 가지 키를 모두 가진다`() {
        val context1 = CoroutineName("jko")
        log(context1[CoroutineName]?.name)
        log(context1[Job]?.isActive)

        val context2 = Job()
        log(context2[CoroutineName]?.name)
        log(context2[Job]?.isActive)

        val context3 = context1 + context2
        log(context3[CoroutineName]?.name)
        log(context3[Job]?.isActive)
    }

    @Test
    fun `CoroutineContext 에 같은 키를 가진 또 다른 원소가 더해지면 맵처럼 새로운 원소가 다른 원소를 대체한다`() {
        val context1 = CoroutineName("jko")
        log(context1[CoroutineName]?.name)

        val context2 = CoroutineName("junhee")
        log(context2[CoroutineName]?.name)

        val context3 = context1 + context2
        log(context3[CoroutineName]?.name)
    }

    @Test
    fun `CoroutineContext 는 컬렉션이라서, 빈 컨텍스트를 만들 수 있다`() {
        val emptyCoroutineContext: CoroutineContext = EmptyCoroutineContext
        log(emptyCoroutineContext[CoroutineName])
        log(emptyCoroutineContext[Job])

        val coroutineContext = emptyCoroutineContext + CoroutineName("jko") + emptyCoroutineContext
        log(coroutineContext[CoroutineName])
    }

    @Test
    fun `원소를 컨텍스트에서 제거할 수 있다`() {
        val coroutineContext1 = CoroutineName("jko") + Job()
        log(coroutineContext1[CoroutineName]?.name)
        log(coroutineContext1[Job]?.isActive)

        val coroutineContext2 = coroutineContext1.minusKey(CoroutineName)
        log(coroutineContext2[CoroutineName]?.name)
        log(coroutineContext2[Job]?.isActive)

        val coroutineContext3 = (coroutineContext1 + CoroutineName("junhee")).minusKey(CoroutineName)
        log(coroutineContext3[CoroutineName]?.name)
        log(coroutineContext3[Job]?.isActive)
    }

    @Test
    fun `자식은 부모로부터 컨텍스트를 상속받는다`() {
        runBlocking(CoroutineName("jko")) {
            log("started")

            val result = async {
                log("async")
                delay(500)
                33
            }

            launch {
                log("launch")
                delay(1000)
            }

            log("before print result")
            log("The answer is ${result.await()}")
        }
    }

    @Test
    fun `모든 자식은 빌더의 인자에서 정의된 특정 컨텍스트를 가질 수 있다`() {
        runBlocking(CoroutineName("jko")) {
            log("started")

            val result = async(CoroutineName("jko-async")) {
                log("async")
                delay(500)
                33
            }

            launch(CoroutineName("jko-launch")) {
                log("launch")
                delay(1000)
            }

            log("before print result")
            log("The answer is ${result.await()}")
        }
    }

    @Test
    fun `coroutineContext 프로퍼티는 모든 중단 스코프에서 사용 가능하다`() {
        runBlocking {
            withContext(CoroutineName("Outer")){
                log(1)
                printName()
                log(2)
                launch(CoroutineName("Inner")) {
                    log(3)
                    printName()
                }
                log(4)
                delay(10)
                log(5)
                printName()
            }
        }
    }

    // 컨텍스트는 중단 함수 사이에 전달되는 continuation 객체가 참조하고 있다.
    private suspend fun printName(){
        log(coroutineContext[CoroutineName]?.name)
    }

    @Test
    fun `CoroutineContext 를 커스텀하게 만드는 방법은 CoroutineContext Element 인터페이스를 구현하는 클래스를 만들면 된다`() {
    }

    class MyCustomContext : CoroutineContext.Element {

        override val key: CoroutineContext.Key<*> = Key

        companion object Key :
            CoroutineContext.Key<MyCustomContext>
    }
}

