package com.minikorp.mini

import org.jetbrains.annotations.TestOnly
import java.io.Closeable
import java.lang.reflect.ParameterizedType
import java.util.concurrent.CopyOnWriteArrayList

/**
 * State holder.
 */
abstract class Store<S> : Closeable, StateContainer<S> {

    companion object {
        private object NoState
    }

    class StoreSubscription internal constructor(private val store: Store<*>,
                                                 private val fn: Any) : Closeable {
        override fun close() {
            store.listeners.remove(fn)
        }
    }

    private var _state: Any? = NoState
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    /**
     * Set new state and notify listeners, only callable from the main thread.
     */
    override fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    fun subscribe(hotStart: Boolean = true, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return StoreSubscription(this, fn)
    }

    override val state: S
        get() {
            if (_state === NoState) {
                synchronized(this) {
                    if (_state === NoState) {
                        _state = initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    @Suppress("UNCHECKED_CAST")
    open fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                as Class<S>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }

    private fun performStateChange(newState: S) {
        //State mutation should to happen on UI thread
        if (newState != _state) {
            _state = newState
            listeners.forEach {
                it(newState)
            }
        }
    }

    /**
     * Test only method, don't use in app code.
     * Will force state change on UI so it can be called from
     * espresso thread.
     */
    @TestOnly
    fun setTestState(s: S) {
        if (isAndroid) {
            onUiSync {
                performStateChange(s)
            }
        } else {
            performStateChange(s)
        }
    }

    final override fun close() {
        listeners.clear() //Remove all listeners
        onClose()
    }

    open fun onClose() = Unit
}
