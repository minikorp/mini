package com.minikorp.mini

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.junit.Test

class StoreFlowTest {

    @Test(timeout = 1000)
    fun `flow sends initial state on collection`(): Unit = runBlocking {
        val store = SampleStore()
        var observedState = SampleStore.INITIAL_STATE

        val job = store.flow(hotStart = false)
                .onEach { observedState = it }
                .take(1)
                .launchIn(GlobalScope)

        store.setState("abc") //Set before collect

        job.join()
        observedState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow sends updates to all`(): Unit = runBlocking {
        val store = SampleStore()
        val called = intArrayOf(0, 0)

        val job1 = store.flow()
                .onEach { called[0]++ }
                .take(2)
                .launchIn(GlobalScope)

        val job2 = store.flow()
                .onEach { called[1]++ }
                .take(2)
                .launchIn(GlobalScope)

        store.setState("abc")

        job1.join()
        job2.join()

        //Called two times, on for initial state, one for updated stated
        called.`should equal`(intArrayOf(2, 2))
        Unit
    }

    @Test(timeout = 1000)
    fun `channel sends updates`(): Unit = runBlocking {
        val store = SampleStore()
        var sentState = ""
        val job = GlobalScope.launch {
            sentState = store.channel().receive()
        }
        store.setState("abc")
        job.join()
        sentState `should be equal to` "abc"
        Unit
    }

    @Test(timeout = 1000)
    fun `flow closes`(): Unit = runBlocking {
        val store = SampleStore()
        var observedState = store.state

        val scope = CoroutineScope(Job())
        store.flow()
                .onEach {
                    observedState = it
                }
                .launchIn(scope)

        scope.cancel() //Cancel the scope
        store.setState("abc")

        observedState `should be equal to` SampleStore.INITIAL_STATE
        Unit
    }
}