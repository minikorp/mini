package mini

import org.amshove.kluent.`should not be empty`
import org.junit.Test

class LoggerMiddlewareTest {


    @Test
    fun `logs are printed`() {
        val store = SampleStore()
        val dispatcher = newTestDispatcher()
        dispatcher.subscribe<TestAction> {
            store.updateState("Action sent")
        }

        val out = StringBuilder()
        dispatcher.addMiddleware(LoggerMiddleware(listOf(store),
            logger = { priority, tag, msg ->
                println("[$priority][$tag] $msg")
                out.append(priority).append(tag).append(msg)
            }))
        dispatcher.dispatchSync(TestAction())
        out.toString().`should not be empty`()
    }

}