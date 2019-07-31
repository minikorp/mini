package mini

import org.amshove.kluent.`should be equal to`
import org.junit.Test

class StoreTest {

    @Test
    fun `state is updated`() {
        val store = SampleStore()
        store.updateState("abc")
        store.state `should be equal to` "abc"
    }

    @Test
    fun `observers are called`() {
        val store = SampleStore()
        var state = ""
        store.subscribe {
            state = it
        }
        store.updateState("abc")
        state `should be equal to` "abc"
    }

    @Test
    fun `initial state is sent on subscribe`() {
        val store = SampleStore()
        var state = ""
        store.subscribe {
            state = it
        }
        state `should be equal to` "initial"
    }

    @Test
    fun `observers are removed on close`() {
        val store = SampleStore()
        var state = ""
        val closeable = store.subscribe(hotStart = false) {
            state = it
        }
        closeable.close()
        store.updateState("abc")
        state `should be equal to` ""
    }
}