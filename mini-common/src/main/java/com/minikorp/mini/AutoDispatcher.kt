package com.minikorp.mini

import java.io.Closeable
import kotlin.reflect.KClass

const val AUTO_STATIC_DISPATCHER = "com.minikorp.AutoGeneratedDispatcher"

interface AutoDispatcher {

    companion object {
        fun get(): AutoDispatcher {
            return Class.forName(AUTO_STATIC_DISPATCHER).getField("INSTANCE").get(null) as AutoDispatcher
        }
    }

    /**
     * All the types an action can be subscribed as.
     */
    val actionTypes: Map<KClass<*>, List<KClass<*>>>

    /**
     * Link all [Reducer] functions present in the store to the dispatcher.
     */
    fun <S> subscribe(dispatcher: Dispatcher, container: StateContainer<S>): Closeable

    /**
     * Link all [Reducer] functions present in the store to the dispatcher.
     */
    fun subscribe(dispatcher: Dispatcher, containers: Iterable<StateContainer<*>>): Closeable {
        val c = CompositeCloseable()
        containers.forEach { container ->
            c.add(subscribe(dispatcher, container))
        }
        return c
    }
}