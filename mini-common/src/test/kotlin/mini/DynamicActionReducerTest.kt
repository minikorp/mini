package mini

import org.junit.Assert.assertEquals
import org.junit.Test

class DynamicActionReducerTest {

    class DummyAction : Action

    @Test
    fun dynamic_action_reducer_calls_subscriptions() {
        val actionReducer = DynamicActionReducer()
        val dummyStore = object : Store<String>() {
            override fun initialize() {
                actionReducer.subscribe(DummyAction::class) { s, dummyAction ->
                    s + "1"
                }
            }
        }
        dummyStore.initialize()
        actionReducer.reduce(DummyAction())
        actionReducer.reduce(DummyAction())
        actionReducer.reduce(DummyAction())
        assertEquals("state is modified 3 times", "111", dummyStore.state)
    }
}