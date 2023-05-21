import org.junit.jupiter.api.Test

class Introduction {

    @Test
    fun `simple thread by providing a lambda`() {
        val thread = Thread {
            println("${Thread.currentThread()} has run.")
        }
        thread.start()
    }

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
    }
}