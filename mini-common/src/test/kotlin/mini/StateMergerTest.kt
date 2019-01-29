package mini

import org.junit.Assert
import org.junit.Test

class StateMergerTest {

    @Test
    fun merge_states_produces_the_correct_state() {
        class DummyStore : Store<String>(Dispatcher()) {
            override fun initialState() = ""
        }

        var result = emptyList<String>()
        val dummyStore = DummyStore()
        val dummyStore2 = DummyStore()
        mergeStates<String> {
            merge(dummyStore) { this }
            merge(dummyStore2) { this }
        }.subscribe {
            result = it
        }
        dummyStore.setTestState("abc")
        Assert.assertEquals("merge emits both values", listOf("abc", ""), result)
    }
}