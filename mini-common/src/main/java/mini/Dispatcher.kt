package mini

import io.reactivex.disposables.Disposable
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
    inline fun <reified A : Any> register(priority: Int = 100, noinline callback: (A) -> Unit): Registration {
        return register(A::class, priority, callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> register(clazz: KClass<T>, priority: Int = 100, callback: (T) -> Unit): Registration {
        synchronized(subscriptions) {
            val reg = Registration(this, clazz.java, priority, callback as (Any) -> Unit)
            val set = subscriptions.getOrPut(clazz.java) {
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
        if (isAndroid) {
            onUiSync { doDispatch(action) }
        } else {
            doDispatch(action)
        }
    }

    @Suppress("NOTHING_TO_INLINE") //Inlined so it doesn't appear in stack trace
    private inline fun doDispatch(action: Any) {
        if (dispatching != null) {
            throw IllegalStateException("Nested dispatch calls. Currently dispatching: " +
                                        "$dispatching. Nested action: $action ")
        }
        dispatching = action
        interceptorChain.proceed(action)
        dispatching = null
    }

    /**
     * Post an event that will dispatch the action on the UI thread
     * and return immediately.
     */
    fun dispatchAsync(action: Any) {
        onUi { dispatch(action) }
    }

    /**
     * Handle for a dispatcher subscription.
     */
    class Registration(val dispatcher: Dispatcher,
                       val type: Class<*>,
                       val priority: Int, val fn: (Any) -> Unit) : Disposable {

        companion object {
            private val idCounter = AtomicInteger()
        }

        val id = idCounter.getAndIncrement()
        var disposed = false

        @Synchronized
        override fun isDisposed(): Boolean = disposed

        @Synchronized
        override fun dispose() {
            dispatcher.unregister(this)
            disposed = true
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Registration
            if (id != other.id) return false
            return true
        }

        override fun hashCode(): Int {
            return id
        }
    }
}