package developersandroid

import org.junit.jupiter.api.Test

class ChallengesWithThreads {

    @Test
    fun `Race conditions and unpredictable behavior`() {
        var count = 0
        for (i in 1..50) {
            Thread {
                count += 1
                println("Thread: $i count: $count")
            }.start()
        }

        Thread.sleep(2000)
    }
}