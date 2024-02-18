package kotlincoroutines

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

class Chapter8 {

    @Test
    fun `자식이 부모로부터 컨텍스트를 물려받는 건 코루틴 빌더의 가장 기본적인 특징이다`() {
        runBlocking(CoroutineName("jko-main")) {
            val name: String? = coroutineContext[CoroutineName]?.name
            log("1: $name")

            launch {
                delay(1000)
                val name: String? = coroutineContext[CoroutineName]?.name
                log("2: $name")
            }
        }
    }

    @Test
    fun `job 의 여러 상태`() {
        runBlocking {
            val job1 = Job()
            log(job1)   // 빌더로 생성된 잡은 완료시킬 때 까지 active

            job1.complete()
            log(job1)

            val job2 = launch {
                delay(100)
            }
            log(job2)
            job2.join() // 코루틴이 완료되는 걸 기다리기 위해 사용한다.
            log(job2)

            val job3 = launch(start = CoroutineStart.LAZY) {
                delay(1000)
            }
            log(job3)
            job3.start()
            log(job3)
            job3.join()
            log(job3)
        }
    }

    @Test
    fun `Job 을 접근하기 쉽게 만들어주는 확장 프로퍼티도 있다`() {
        runBlocking {
            val job1: Job? = coroutineContext[Job]
            log(job1)

            val job2: Job = coroutineContext.job
            log(job2)
        }
    }

    // 모든 코루틴은 자신만의 Job 을 생성한다.
    // 인자 또는 부모 코루틴으로부터 온 잡은 새로운 잡의 부모로 사용된다.
    @Test
    fun `Job 은 코루틴이 상속하지 않는 유일한 코루틴 컨텍스트이다`() {
        runBlocking {
            val parentName = CoroutineName("jko")
            val parentJob = Job()

            launch(parentName + parentJob) {
                val childName = coroutineContext[CoroutineName]
                log(childName)
                log("parentName == childName: ${parentName == childName}")

                val childJob = coroutineContext[Job]
                log(childJob)
                log("parentJob == childJob: ${parentJob == childJob}")

                log("childJob == parentJob.children.first() : ${childJob == parentJob.children.first()}")
                log("childJob.parent == parentJob : ${childJob?.parent == parentJob}")
            }
        }
    }

    // 부모와 자식과 관계가 없다.
    // 그래서, 부모가 자식 코루틴을 기다리지 않는다.
    // 자식은 인자로 들어온 잡을 부모로 사용하기 때문에, runBlocking 과는 아무런 관계가 없다.
    @Test
    fun `새로운 Job 컨텍스트가 부모의 잡을 대체하면, 구조화된 동시성이 작동하지 않는다`() {
        runBlocking {
            launch(Job()) {
                delay(1000L)
                log("will not be printed")
            }
        }
    }

    @Test
    fun `join 은 지정한 잡이 completed 나 cancelled 같은 마지막 상태에 도달할 때 까지 기다리는 중단 함수이다`() {
        runBlocking {
            val job1 = launch {
                log("first launch")
                delay(1000)
                log("1")
            }
            val job2 = launch {
                log("second launch")
                delay(2000)
                log("2")
            }

            log("before join")
            job1.join()
            job2.join()
            log("after join")
        }
    }

    // 모든 자식이 마지막 상태가 될 때까지 기다리는데 활용할 수 있다.
    @Test
    fun `Job 인터페이스에는 모든 자식을 참조할 수 있는 children 프로퍼티도 있다`() {
        runBlocking {
            launch {
                delay(1000)
                log("1")
            }
            launch {
                delay(2000)
                log("2")
            }

            val job: Job? = coroutineContext[Job]
            val children: Sequence<Job>? = job?.children

            log("Number of children: ${children?.count()}")
            children?.forEach { it.join() }
            log("DONE")
        }
    }

    // 흔히 하는 실수가, Job() 팩토리 함수를 사용해 잡을 생성하고, 다른 코루틴의 부모로 지정한 뒤에 join 을 호출하는 것이다.
    @Test
    fun `자식 코루틴이 모두 작업을 마쳐도 Job 이 여전히 active 상태인 경우`() {
        runBlocking {
            val job = Job()

            // 새로운 잡이 부모로부터 상속 받은 잡을 대체
            launch(job) {
                delay(100)
                log("1")
            }
            launch(job) {
                delay(200)
                log("2")
            }

            job.join()  // 여기서 영원히 대기 -> 자식 코루틴이 모두 끝나도 job 은 여전히 active 상태이기 떄문에
            log("DONE")
        }
    }

    @Test
    fun `잡의 모든 자식 코루틴에서 join 을 호출해야한다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                delay(1000)
                log("1")
            }
            launch(job) {
                delay(2000)
                log("2")
            }

            job.children.forEach { it.join() }
        }
    }

    @Test
    fun `CompletableJob 의 complete 는 잡을 완료하는데 사용된다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                log("1")
                repeat(5) {num ->
                    delay(200)
                    log("repeat: $num")
                }
            }

            launch {
                log("2")
                delay(300L)
                job.complete()   // 모든 자식 코루틴은 작업을 완료할 때 까지 실행된 상태 유지
            }

            log("before join")
            job.join()
            log("after join")

            launch(job) {
                log("will not be printed")
            }
        }
    }

    @Test
    fun `CompletableJob 의 completeExceptionally 는 인자로 받은 예외로 잡을 완료시킨다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                repeat(5) { num ->
                    delay(200)
                    log("repeat: ${num}")
                }
            }

            launch {
                log("--")
                delay(500)
                log("==")
                job.completeExceptionally(Error("My Error"))
            }

            job.join()

            launch(job) {
                log("will not be printed ")
            }

            log("DONE")
        }
    }

    @Test
    fun `complete 는 잡의 마지막 코루틴을 시작한 후 자주 사용된다`() {
        runBlocking {
            val job = Job()

            launch(job) {
                delay(100)
                log("1")
            }

            launch(job) {
                delay(200)
                log("2")
            }

            job.complete()
            job.join()
        }
    }

    @Test
    fun `부모 잡이 취소되면 해당 잡 또한 취소된다`() {
        runBlocking {
            val parentJob = Job()
            val job = Job(parentJob)

            launch(job) {
                delay(100)
                log("1")
            }
            launch(job) {
                delay(200)
                log("2")
            }

            delay(150)
            parentJob.cancel()
            job.children.forEach { it.join() }
        }
    }
}