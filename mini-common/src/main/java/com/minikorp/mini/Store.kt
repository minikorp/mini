package com.minikorp.mini

import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Basic state holder.
 */
abstract class Store<S> : Closeable,
        StateContainer<S>,
        CloseableTracker by DefaultCloseableTracker() {

    class StoreSubscription internal constructor(private val store: Store<*>,
                                                 private val fn: Any) : Closeable {
        override fun close() {
            store.listeners.remove(fn)
        }
    }

    private var _state: Any? = StateContainer.Companion.NoState
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    /**
     * Set new state and notify listeners, only callable from the main thread.
     */
    override fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return StoreSubscription(this, fn)
    }

    override val state: S
        get() {
            if (_state === StateContainer.Companion.NoState) {
                synchronized(this) {
                    if (_state === StateContainer.Companion.NoState) {
                        _state = initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    private fun performStateChange(newState: S) {
        //State mutation should to happen on UI thread
        if (_state != newState) {
            _state = newState
            listeners.forEach {
                it(newState)
            }
        }
    }

    override fun close() {
        listeners.clear() //Remove all listeners
        close()
    }

}
