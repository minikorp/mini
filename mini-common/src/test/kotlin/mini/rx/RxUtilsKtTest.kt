package mini.rx

import mini.SampleStore
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class RxUtilsKtTest {
    @Test
    fun `flowable sends initial state`() {
        val store = SampleStore()
        store.updateState("abc") //Set before subscribe
        var sentState = ""
        store.flowable().subscribe {
            sentState = it
        }
        sentState `should be equal to` "abc"
    }

    @Test
    fun `flowable sends updates`() {
        val store = SampleStore()
        var sentState = ""
        store.flowable().subscribe {
            sentState = it
        }
        store.updateState("abc") //Set before subscribe
        sentState `should be equal to` "abc"
    }

    @Test
    fun `observable sends initial state`() {
        val store = SampleStore()
        store.updateState("abc") //Set before subscribe
        var sentState = ""
        store.observable().subscribe {
            sentState = it
        }
        sentState `should be equal to` "abc"
    }

    @Test
    fun `observable sends updates`() {
        val store = SampleStore()
        var sentState = ""
        store.observable().subscribe {
            sentState = it
        }
        store.updateState("abc") //Set before subscribe
        sentState `should be equal to` "abc"
    }
}