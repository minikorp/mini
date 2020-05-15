package com.minikorp.mini

import kotlinx.coroutines.Dispatchers
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

const val AUTO_STATIC_DISPATCHER = "com.minikorp.AutoStaticDispatcher"

interface StaticDispatcher {

    companion object {
        /**
         * Create a new dispatcher with already configured subscriptions scanned during code generation.
         */
        fun create(containers: Iterable<StateContainer<*>>,
                   actionDispatchContext: CoroutineContext? = null,
                   strictMode: Boolean = false): Dispatcher {

            val generatedStaticDispatcher =
                    Class.forName(AUTO_STATIC_DISPATCHER).getField("INSTANCE").get(null) as StaticDispatcher

            val dispatcher = Dispatcher(
                    actionTypes = generatedStaticDispatcher.actionTypes,
                    actionDispatchContext = actionDispatchContext,
                    strictMode = strictMode
            )

            generatedStaticDispatcher.subscribe(dispatcher, containers)
            return dispatcher
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