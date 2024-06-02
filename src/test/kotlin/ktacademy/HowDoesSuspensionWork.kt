package ktacademy

import kotlinx.coroutines.*
import log
import org.junit.jupiter.api.Test

// https://kt.academy/article/cc-suspension

class HowDoesSuspensionWork {

    /*
    * Take a look again at the suspendCancellableCoroutine invocation
    * and notice that it ends with a lambda expression ({ }).
    * The function passed as an argument will be invoked before the suspension.
    * */
    @Test
    fun `coroutine is suspended after logging 'before' and never resumed`() {
        runBlocking {
            log("Before suspendCancellableCoroutine")
            suspendCancellableCoroutine<Unit> {
                log("In suspendCancellableCoroutine")
            }
            log("After suspendCancellableCoroutine")
        }
    }

    /*
    * Continuation is the object that we can use to resume the coroutine.
    * We could use it to resume immediately.
    *
    * This statement is true, but I need to clarify.
    * You might imagine that here we suspend and immediately resume.
    * This is a good intuition, but the truth is that there is an optimization that prevents a suspension if resuming is immediate.
    * */
    @Test
    fun resume() {
        runBlocking {
            log("Before suspendCancellableCoroutine")
            suspendCancellableCoroutine<Unit> { continuation->
                log("Before resume in suspendCancellableCoroutine")
                continuation.resume(Unit, null)
            }
            log("After suspendCancellableCoroutine")
        }
    }

    @Test
    fun `myTest - do sth when suspend coroutine`() {
        runBlocking {
            log("Before launch")
            launch {
                log("In launch")
            }
            log("Before suspendCancellableCoroutine")
            suspendCancellableCoroutine<Unit> { continuation ->
                log("In suspendCancellableCoroutine")
            }
            log("After suspendCancellableCoroutine")
        }
    }

    @Test
    fun `myTest - cancel when suspend coroutine`() {
        runBlocking {
            log("Before launch")
            launch {
                log("In launch")

                val myJob = this.coroutineContext.job
                val parentJob: Job? = myJob.parent

                log("myJob: ${myJob}, parentJob: ${parentJob}")

                parentJob?.cancelAndJoin()
            }
            log("After launch")

            log("Before suspendCancellableCoroutine")
            suspendCancellableCoroutine<Unit> { continuation ->
                log("In suspendCancellableCoroutine")
            }
            log("After suspendCancellableCoroutine")
        }
    }

}