package mini.rx

import mini.SampleStore
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class RxExTest {

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

    @Test
    fun `observable completes`() {
        val store = SampleStore()
        var sentState = ""
        val disposable = store.observable(hotStart = false).subscribe {
            sentState = it
        }
        disposable.dispose() //Clear it
        store.updateState("abc")
        sentState `should be equal to` "" //No change should be made
    }
}