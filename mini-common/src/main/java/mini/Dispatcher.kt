package mini

import java.util.*

/**
 * Hub for actions.
 *
 * @param actionTypes All types an action can be observed as.
 * If map is empty, the runtime type itself will be used. If using code generation,
 * Mini.actionTypes will contain a map with all super types of @Action annotated classes.
 */
class Dispatcher {

    var actionTypes: Map<Class<*>, List<Class<*>>> = emptyMap()

    private val subscriptionCaller: Chain = object : Chain {
        override fun proceed(action: Any): Any {
            synchronized(subscriptions) {
                val types = actionTypes[action::class.java] ?: listOf(action::class.java)
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
    val subscriptions: MutableMap<Class<*>, MutableSet<Registration>> = HashMap()

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

    @Suppress("UNCHECKED_CAST")
    inline fun <reified A> register(priority: Int = 100, noinline callback: (A) -> Unit): Registration {
        synchronized(subscriptions) {
            val reg = Registration(A::class.java, priority, callback as (Any) -> Unit)
            val set = subscriptions.getOrPut(A::class.java) {
                TreeSet(kotlin.Comparator { a, b -> a.priority.compareTo(b.priority) })
            }
            set.add(reg)
            return reg
        }
    }

    fun unregister(registration: Registration) {
        synchronized(subscriptions) {
            subscriptions[registration.type]?.remove(registration)
        }
    }

    /**
     * Dispatch an action on the main thread synchronously.
     * This method will block the caller if it's not
     * the main thread.
     */
    fun dispatch(action: Any) {
        onUiSync {
            if (dispatching != null) {
                throw IllegalStateException("Nested dispatch calls. Currently dispatching: " +
                                            "$dispatching. Nested action: $action ")
            }
            dispatching = action
            interceptorChain.proceed(action)
            dispatching = null
        }
    }

    /**
     * Post an event that will dispatch the action on the UI thread
     * and return immediately.
     */
    fun dispatchAsync(action: Any) {
        onUi { dispatch(action) }
    }

    data class Registration(val type: Class<*>, val priority: Int, val fn: (Any) -> Unit)
}