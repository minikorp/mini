package mini

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.junit.Test

class DispatcherContainerTest {

    class DummyContainer(override val dispatcher: Dispatcher) : DispatcherContainer {
        override val defaultDispatchScope: CoroutineScope
            get() = GlobalScope
    }

    @Test
    fun `on complete is called`() {
        val dispatcher = newTestDispatcher()
        val container = DummyContainer(dispatcher)
        val dispatchSignals = Channel<Unit>(0)

        var called = false
        dispatcher.subscribe<TestAction> {
            dispatchSignals.send(Unit)
            delay(300)
            dispatchSignals.send(Unit)
            called = true
        }

        runBlocking {
            container.dispatch(TestAction()) {
                called.`should be true`()
            }
            dispatchSignals.receive() //Wait for dispatch to start
            called.`should be false`()
            dispatchSignals.receive() //Wait for dispatch to complete

        }
    }
}