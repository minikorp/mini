package mini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

private typealias DispatchCallback = suspend (Any) -> Unit


/**
 * Hub for actions. Use code generation with ```MiniGen.newDispatcher()```
 * or provide action type map information.
 *
 * @param actionTypes All types an action can be observed as.
 * If map is empty, the runtime type itself will be used. If using code generation,
 * Mini.actionTypes will contain a map with all super types of @Action annotated classes.
 *
 * @param strictMode Verify calling thread, only disable in production!
 *
 */
class Dispatcher(private val actionTypes: Map<KClass<*>, List<KClass<*>>>,
                 private val actionDispatchContext: CoroutineContext = Dispatchers.Main.immediate,
                 private val strictMode: Boolean = false) {

    private val subscriptionCaller: Chain = object : Chain {
        override suspend fun proceed(action: Any): Any {
            val types = actionTypes[action::class]
                ?: error("${action::class.simpleName} is not action")
            //Ensure reducer is called on Main dispatcher
            types.forEach { type ->
                subscriptions[type]?.forEach { it.fn(action) }
            }
            return action
        }
    }

    private val middlewares: MutableList<Middleware> = ArrayList()
    private var middlewareChain: Chain = buildChain()
    private val actionStack: Stack<Any> = Stack()
    val lastAction: Any? get() = actionStack.firstOrNull()

    internal val subscriptions: MutableMap<KClass<*>, MutableSet<DispatcherSubscription>> = HashMap()

    private fun buildChain(): Chain {
        return middlewares.fold(subscriptionCaller) { chain, middleware ->
            object : Chain {
                override suspend fun proceed(action: Any): Any {
                    return middleware.intercept(action, chain)
                }
            }
        }
    }

    fun addMiddleware(middleware: Middleware) {
        synchronized(this) {
            middlewares += middleware
            middlewareChain = buildChain()
        }
    }

    fun removeInterceptor(middleware: Middleware) {
        synchronized(this) {
            middlewares -= middleware
            middlewareChain = buildChain()
        }
    }

    inline fun <reified A : Any> subscribe(priority: Int = 100,
                                           noinline callback: suspend (A) -> Unit): Closeable {
        return subscribe(A::class, priority, callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> subscribe(clazz: KClass<T>,
                            priority: Int = 100,
                            callback: suspend (T) -> Unit): Closeable {
        synchronized(subscriptions) {
            val reg = DispatcherSubscription(this, clazz, priority, callback as DispatchCallback)
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
     * Dispatch an action using the provided [actionDispatchContext],
     * defaults to [Dispatchers.Main] for Android.
     *
     * @see [dispatchSync].
     */
    suspend fun dispatch(action: Any, context: CoroutineContext = EmptyCoroutineContext) {
        withContext(context + actionDispatchContext) {
            doDispatch(action)
        }
    }

    /**
     * Dispatch an action, blocking the thread until it's complete.
     *
     * Calling from UI thread will result will throw an exception since it can potentially result
     * in ANR error.
     */
    fun dispatchSync(action: Any, context: CoroutineContext = EmptyCoroutineContext) {
        if (strictMode) assertOnBgThread()
        runBlocking {
            dispatch(action, context = context)
        }
    }

    private suspend fun doDispatch(action: Any) {
        actionStack.push(action)
        middlewareChain.proceed(action)
        actionStack.pop()
    }

    /**
     * Handle for a dispatcher subscription.
     */
    data class DispatcherSubscription internal constructor(val dispatcher: Dispatcher,
                                                           val type: KClass<*>,
                                                           val priority: Int,
                                                           val fn: DispatchCallback) : Closeable {
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