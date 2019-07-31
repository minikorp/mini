package mini

import java.io.Closeable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

/**
 * Hub for actions.
 *
 * @param actionTypes All types an action can be observed as.
 * If map is empty, the runtime type itself will be used. If using code generation,
 * Mini.actionTypes will contain a map with all super types of @Action annotated classes.
 */
class Dispatcher(val actionTypes: Map<KClass<*>, List<KClass<*>>> = emptyMap()) {

    private val subscriptionCaller: Chain = object : Chain {
        override fun proceed(action: Any): Any {
            synchronized(subscriptions) {
                val types = actionTypes[action::class] ?: listOf(action::class)
                types.forEach { type ->
                    subscriptions[type]?.forEach { it.fn(action) }
                }
            }
            return action
        }
    }

    private val interceptors: MutableList<Interceptor> = ArrayList()
    private var interceptorChain: Chain = buildChain()
    private var dispatching: Any? = null
    internal val subscriptions: MutableMap<KClass<*>, MutableSet<DispatcherSubscription>> = HashMap()

    private fun buildChain(): Chain {
        return interceptors.fold(subscriptionCaller) { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Any): Any {
                    return interceptor(action, chain)
                }
            }
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            interceptorChain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            interceptorChain = buildChain()
        }
    }

    inline fun <reified A : Any> subscribe(priority: Int = 100, noinline callback: (A) -> Unit): Closeable {
        return subscribe(A::class, priority, callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> subscribe(clazz: KClass<T>, priority: Int = 100, callback: (T) -> Unit): Closeable {
        synchronized(subscriptions) {
            val reg = DispatcherSubscription(this, clazz, priority, callback as (Any) -> Unit)
            val set = subscriptions.getOrPut(clazz) {
                TreeSet(kotlin.Comparator { a, b ->
                    //Sort by priority, then by id for equal priority
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                })
            }
            set.add(reg)
            return reg
        }
    }

    fun unregister(registration: DispatcherSubscription) {
        synchronized(subscriptions) {
            subscriptions[registration.type]?.remove(registration)
        }
    }

    /**
     * Dispatch an action on the main thread synchronously.
     * This method will block the caller until all listeners have handled the event,
     * usually the main thread.
     *
     * Use [dispatchAsync] to avoid this.
     */
    fun dispatch(action: Any) {
        if (isAndroid) {
            onUiSync { doDispatch(action) }
        } else {
            doDispatch(action)
        }
    }

    /**
     * Post an event that will dispatch the action on the UI thread
     * and return immediately.
     */
    fun dispatchAsync(action: Any) {
        if (isAndroid) {
            onUi { dispatch(action) }
        } else {
            dispatch(action) //Just dispatch it
        }
    }

    private fun doDispatch(action: Any) {
        if (dispatching != null) {
            throw IllegalStateException("Nested dispatch calls. Currently dispatching: " +
                                        "$dispatching. Nested action: $action ")
        }
        dispatching = action
        interceptorChain.proceed(action)
        dispatching = null
    }

    /**
     * Handle for a dispatcher subscription.
     */
    data class DispatcherSubscription internal constructor(val dispatcher: Dispatcher,
                                                           val type: KClass<*>,
                                                           val priority: Int, val fn: (Any) -> Unit) : Closeable {
        companion object {
            private val idCounter = AtomicInteger()
        }

        override fun close() {
            if (disposed) return
            dispatcher.unregister(this)
            disposed = true
        }

        //Alias for close
        fun dispose() = close()

        val id = idCounter.getAndIncrement()
        var disposed = false
    }
}