package com.minivac.mini.redux

import com.minivac.mini.flux.Action
import com.minivac.mini.flux.Store
import com.minivac.mini.misc.assertOnUiThread
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap
import kotlin.reflect.KClass

const val DEFAULT_PRIORITY = 100

typealias ReduceFn<S, A> = (S, A) -> S

abstract class ReduxStore<S : Any> : Store<S>() {

    private val reducerCounter = AtomicInteger()
    private val reducers: MutableMap<Class<*>, TreeSet<Reducer<S>>?> = HashMap()

    fun <A : Any> addReducer(priority: Int = DEFAULT_PRIORITY,
                                tag: KClass<A>,
                                fn: ReduceFn<S, A>) {
        @Suppress("UNCHECKED_CAST")
        val reducer = Reducer(reducerCounter.getAndIncrement(),
                priority,
                fn as ReduceFn<S, Any>)

        synchronized(this) {
            reducers.getOrPut(tag.java, {
                TreeSet({ a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                })
            })!!.add(reducer)
        }
    }

    /**
     * Dispatch an action. Can only be called from UI thread.
     */
    fun dispatch(action: Action) {
        assertOnUiThread()
        val newState = action.tags
                .map { reducers[it] }
                .filterNotNull()
                .fold(state) { acc, reducers ->
                    reducers.fold(acc, { acc, reducer -> reducer.fn.invoke(acc, action) })
                }
        state = newState
    }
}

private data class Reducer<S>(
        val id: Int,
        val priority: Int,
        val fn: ReduceFn<S, Any>)