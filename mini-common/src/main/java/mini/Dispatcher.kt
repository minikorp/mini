package mini

import mini.log.Grove
import java.util.*

class Dispatcher {
    companion object {
        const val ACTION_TYPES_GEN_PACKAGE = "mini"
        const val ACTION_TYPES_GEN_CLASS = "ActionTypes"
    }

    private val interceptors: MutableList<Interceptor> = ArrayList()
    private var interceptorChain: Chain = buildChain()
    private var dispatching: Any? = null
    val subscriptions: MutableMap<Class<*>, MutableSet<Registration>> = HashMap()
    val actionTypes: Map<Class<*>, List<Class<*>>>

    init {
        try {
            val clazz = Class.forName("$ACTION_TYPES_GEN_PACKAGE.$ACTION_TYPES_GEN_CLASS")
            @Suppress("UNCHECKED_CAST")
            actionTypes = clazz.getDeclaredField("actionTypes").get(null)
                as Map<Class<*>, List<Class<*>>>
        } catch (ex: ClassNotFoundException) {
            Grove.e { "Code was not properly generated" }
            throw ex
        }
    }

    private val subscriptionCaller: Chain = object : Chain {
        override fun proceed(action: Any): Any {
            synchronized(subscriptions) {

                //                subscriptions[Any::class.java]?.forEach { it.fn(action) }
//                subscriptions[action.javaClass]?.forEach { it.fn(action) }
//                action.types().forEach { type ->
//                    subscriptions[type]?.forEach { it.fn(action) }
//                }
            }
            return action
        }
    }

    private fun buildChain(): Chain {
        return interceptors.fold(subscriptionCaller)
        { chain, interceptor ->
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
    inline fun <reified A : Action> register(priority: Int = 100, noinline callback: (A) -> Unit): Registration {
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