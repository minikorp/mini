package com.minikorp.mini

import java.io.Closeable

/**
 * Utility class to allow splitting [StateContainer] into chunks so not all reducers live in the same
 * file.
 *
 * From a state container
 *
 * ```
 * class Reducer : NestedStateContainer<State>() {
 *      @Reducer
 *      fun reduceOneAction(...)
 * }
 *
 * class MyStore {
 *      val reducer = Reducer(this)
 *
 *      init {
 *          Mini.link(dispatcher, listOf(this, reducer))
 *      }
 *
 *      @Reducer
 *      fun globalReduceFn(...)
 * }
 * ```
 */
abstract class NestedStateContainer<S : Any>(var parent: StateContainer<S>? = null) : StateContainer<S> {
    override val state: S
        get() = parent!!.state

    override fun setState(newState: S) {
        parent!!.setState(newState)
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        return parent!!.subscribe(hotStart, fn)
    }
}