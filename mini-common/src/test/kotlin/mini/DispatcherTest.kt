package mini

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class DispatcherTest {

    class TestAction : BaseAction()

    @Test
    fun `subscriptions are added`() {
        val dispatcher = Dispatcher()
        var called = 0
        dispatcher.subscribe<TestAction> {
            called++
        }
        dispatcher.dispatch(TestAction())
        called `should be equal to` 1
    }

    @Test
    fun `order is respected for same priority`() {
        val dispatcher = Dispatcher()
        val calls = ArrayList<Int>()
        dispatcher.subscribe<TestAction> {
            calls.add(0)
        }
        dispatcher.subscribe<TestAction> {
            calls.add(1)
        }
        dispatcher.dispatch(TestAction())
        calls[0] `should be equal to` 0
        calls[1] `should be equal to` 1
    }

    @Test
    fun `order is respected for different priority`() {
        val dispatcher = Dispatcher()
        val calls = ArrayList<Int>()
        dispatcher.subscribe<TestAction>(priority = 10) {
            calls.add(0)
        }
        dispatcher.subscribe<TestAction>(priority = 0) {
            calls.add(1)
        }
        dispatcher.dispatch(TestAction())
        calls[0] `should be equal to` 1
        calls[1] `should be equal to` 0
    }

    @Test
    fun `disposing registration removes subscription`() {
        val dispatcher = Dispatcher()
        var called = 0
        dispatcher.subscribe<TestAction> {
            called++
        }.close()
        dispatcher.dispatch(TestAction())
        called `should be equal to` 0
    }

    @Test
    fun `interceptors are called`() {
        val dispatcher = Dispatcher()
        var called = 0
        val interceptor: Interceptor = { action, chain ->
            called++
            chain.proceed(action)
        }
        dispatcher.addInterceptor(interceptor)
        dispatcher.dispatch(TestAction())
        called `should be equal to` 1
    }
}