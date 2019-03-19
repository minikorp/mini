package mini

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class DispatcherTest {

    class TestAction : BaseAction()

    @Test
    fun subscriptions_are_added() {
        val dispatcher = Dispatcher()
        var called = 0
        dispatcher.register<TestAction> {
            called++
        }
        dispatcher.dispatch(TestAction())
        called `should be equal to` 1
    }

    @Test
    fun order_is_respected_for_same_priority() {
        val dispatcher = Dispatcher()
        val calls = ArrayList<Int>()
        dispatcher.register<TestAction> {
            calls.add(0)
        }
        dispatcher.register<TestAction> {
            calls.add(1)
        }
        dispatcher.dispatch(TestAction())
        calls[0] `should be equal to` 0
        calls[1] `should be equal to` 1
    }

    @Test
    fun order_is_respected_for_different_priority() {
        val dispatcher = Dispatcher()
        val calls = ArrayList<Int>()
        dispatcher.register<TestAction>(priority = 10) {
            calls.add(0)
        }
        dispatcher.register<TestAction>(priority = 0) {
            calls.add(1)
        }
        dispatcher.dispatch(TestAction())
        calls[0] `should be equal to` 1
        calls[1] `should be equal to` 0
    }

    @Test
    fun disposing_registration_removes_subscription() {
        val dispatcher = Dispatcher()
        var called = 0
        dispatcher.register<TestAction> {
            called++
        }.dispose()
        dispatcher.dispatch(TestAction())
        called `should be equal to` 0
    }
}