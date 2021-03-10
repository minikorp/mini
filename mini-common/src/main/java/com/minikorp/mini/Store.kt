package com.minikorp.mini

import java.io.Closeable
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Store<S : Any>(
        initialState: S,
        val storeScope: CoroutineScope,
        val reducer: Reducer<S>) : Closeable {

    class StoreSubscription internal constructor(private val store: Store<*>,
                                                 private val fn: Any) : Closeable {
        override fun close() {
            store.listeners.remove(fn)
        }
    }

    private var _state: S = initialState
    val state: S get() = _state
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    /**
     * Set new state and notify listeners, only callable from the main thread.
     */
    fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return StoreSubscription(this, fn)
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

    private val callerChain = object : Chain<S> {
        override suspend fun proceed(context: DispatchContext<S>) {
            setState(reducer.reduce(state, context.action))
        }
    }

    private val middlewares: MutableList<Middleware<S>> = ArrayList()
    private var middlewareChain: Chain<S> = buildChain()

    fun addMiddleware(middleware: Middleware<S>) {
        synchronized(this) {
            middlewares.add(0, middleware)
            middlewareChain = buildChain()
        }
    }

    private fun buildChain(): Chain<S> {
        return middlewares.fold(callerChain as Chain<S>) { chain, middleware ->
            object : Chain<S> {
                override suspend fun proceed(context: DispatchContext<S>) {
                    middleware.intercept(context, chain)
                }
            }
        }
    }

    suspend fun dispatch(action: Action) {
        middlewareChain.proceed(DispatchContext(this, action))
    }

    fun offer(action: Action): Job {
        return storeScope.launch { dispatch(action) }
    }

    override fun close() {
        listeners.clear() //Remove all listeners
    }
}