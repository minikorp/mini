package com.minikorp.mini.test

import com.minikorp.mini.AutoDispatcher
import com.minikorp.mini.Dispatcher
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should equal`
import org.junit.Test


internal class ReducersStoreTest {

    private val store = ReducersStore()
    private val dispatcher = Dispatcher(
            actionTypes = AutoDispatcher.get().actionTypes
    ).apply {
        AutoDispatcher.get().subscribe(this, store)
    }

    @Test
    fun `pure reducers are called`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("changed"))
            store.state.value.`should equal`("changed")
        }
    }

    @Test
    fun `pure static reducers are called`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("changed"))
            store.state.value.`should equal`("changed")
        }
    }
}