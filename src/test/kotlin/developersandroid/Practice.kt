package developersandroid

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test

class Practice {

    @Test
    fun `Creating and running multiple threads`() {
        val states = arrayOf("Starting", "Doing Task 1", "Doing Task 2", "Ending")

        repeat(3) {
            Thread {
                println("${Thread.currentThread()} has started")
                for (i in states) {
                    println("${Thread.currentThread()} - $i")
                    Thread.sleep(50)
                }
            }.start()
        }

        Thread.sleep(3000)
    }

    // asis: Creating and running multiple threads
    @Test
    fun `rewrite the code to use coroutines instead of Thread`() {
        val states = arrayOf("Starting", "Doing Task 1", "Doing Task 2", "Ending")

        repeat(10) {
            GlobalScope.launch {
                println("${Thread.currentThread()} has started")
                for (i in states) {
                    println("${Thread.currentThread()} - $i")
                }
            }
        }

        Thread.sleep(3000)
    }
}