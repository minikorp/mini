package com.minikorp.mini

import org.jetbrains.annotations.TestOnly
import java.io.Closeable
import java.lang.reflect.ParameterizedType

/**
 * Common interface for state containers.
 */
interface StateContainer<S> {

    companion object {
        /**
         * Token to mark a state as not initialized.
         */
        internal object NoState
    }

    val state: S

    fun setState(newState: S)

    /**
     * Register a observer to state changes.
     *
     * @return [Closeable] to cancel the subscription.
     */
    fun subscribe(hotStart: Boolean = true, fn: (S) -> Unit): Closeable

    /**
     * The initial state of the container. By default it will invoke the primary constructor
     * of the State type parameter. If this constructor is not accessible provide your own
     * implementation of this method.
     */
    @Suppress("UNCHECKED_CAST")
    fun initialState(): S {
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

    /**
     * Test only method, don't use in app code.
     * Will force state change on UI so it can be called from
     * espresso thread.
     */
    @TestOnly
    fun setTestState(s: S) {
        if (isAndroid) {
            onUiSync {
                setState(s)
            }
        } else {
            setState(s)
        }
    }
}

