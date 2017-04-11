package com.minivac.mini.flux

import com.minivac.mini.BuildConfig
import com.minivac.mini.log.Grove
import com.minivac.mini.misc.assertNotOnUiThread
import com.minivac.mini.misc.assertOnUiThread
import com.minivac.mini.misc.onUi
import com.minivac.mini.misc.onUiSync
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * Common interface for all actions.
 * Tags must be types that this action implements.
 * Defaults to Any and the runtime type.
 */
interface Action {
    val tags: Array<Class<*>>
        get() = arrayOf(Any::class.java, this.javaClass)
}

/**
 * Debug Action that captures trace information when its created.
 * It has no effect on release builds.
 */
abstract class TracedAction : Action {
    val trace: Array<StackTraceElement>? = let {
        if (BuildConfig.DEBUG) {
            Throwable().stackTrace
        } else {
            null
        }
    }
}


typealias Interceptor = (Action, Chain) -> Action

/**
 * A chain of interceptors. Call [.proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    fun proceed(action: Action): Action
}


class Dispatcher(private val verifyThreads: Boolean = true) {
    val DEFAULT_PRIORITY: Int = 100

    val subscriptionCount: Int get() = subscriptionMap.values.map { it?.size ?: 0 }.sum()
    var dispatching: Boolean = false
        private set

    private val subscriptionMap = HashMap<Class<*>, TreeSet<DispatcherSubscription<Any>>?>()
    private var subscriptionCounter = AtomicInteger()

    private val interceptors = ArrayList<Interceptor>()
    private val rootChain: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            action.tags.forEach { tag ->
                subscriptionMap[tag]?.let { set ->
                    set.forEach { it.cb.invoke(action) }
                }
            }
            return action
        }
    }
    private var chain = rootChain
    private fun buildChain(): Chain {
        return interceptors.fold(rootChain)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action {
                    return interceptor(action, chain)
                }
            }
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            chain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            chain = buildChain()
        }
    }

    fun dispatch(action: Action) {
        if (verifyThreads) assertOnUiThread()
        synchronized(this) {
            try {
                if (dispatching) error("Can't dispatch actions while reducing state!")
                dispatching = true
                chain.proceed(action)
            } finally {
                dispatching = false
            }
        }
    }

    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    fun dispatchOnUiSync(action: Action) {
        if (verifyThreads) assertNotOnUiThread()
        onUiSync { dispatch(action) }
    }

    fun <T : Any> observable(tag: KClass<T>, fn: (Observable<T>) -> Unit)
            = observable(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> observable(priority: Int = DEFAULT_PRIORITY,
                             tag: KClass<T>,
                             fn: (Observable<T>) -> Unit): DispatcherSubscription<T> {
        val subject = PublishSubject.create<T>()
        val subscription = DispatcherSubscription.ObservableSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                subject)
        fn.invoke(subject)
        return registerInternal(subscription)
    }

    fun <T : Any> flowable(tag: KClass<T>, fn: (Flowable<T>) -> Unit)
            = flowable(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> flowable(priority: Int,
                           tag: KClass<T>,
                           fn: (Flowable<T>) -> Unit): DispatcherSubscription<T> {
        val processor = PublishProcessor.create<T>()
        val subscription = DispatcherSubscription.FlowableSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                processor)
        fn.invoke(processor)
        return registerInternal(subscription)
    }

    fun <T : Any> callback(tag: KClass<T>, fn: (T) -> Unit)
            = callback(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> callback(priority: Int,
                           tag: KClass<T>,
                           fn: (T) -> Unit): DispatcherSubscription<T> {
        val subscription = DispatcherSubscription.CallbackSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                fn)
        return registerInternal(subscription)
    }

    internal fun <T : Any> registerInternal(dispatcherSubscription: DispatcherSubscription<T>): DispatcherSubscription<T> {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            subscriptionMap.getOrPut(dispatcherSubscription.tag, {
                TreeSet({ a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                })
            })!!.add(dispatcherSubscription as DispatcherSubscription<Any>)
        }
        return dispatcherSubscription
    }

    internal fun <T : Any> unregisterInternal(dispatcherSubscription: DispatcherSubscription<T>) {
        synchronized(this) {
            val set = subscriptionMap[dispatcherSubscription.tag] as? TreeSet<*>
            val removed = set?.remove(dispatcherSubscription) ?: false
            if (!removed) {
                Grove.w { "Failed to remove dispatcherSubscription, multiple dispose calls?" }
            }
        }
    }
}

sealed class DispatcherSubscription<T : Any>(internal val dispatcher: Dispatcher,
                                             internal val id: Int,
                                             internal val priority: Int,
                                             internal val tag: Class<T>,
                                             internal val cb: (T) -> Unit) : Disposable {

    private var disposed = false
    override fun isDisposed(): Boolean = disposed

    protected fun disposeInternal() {
        dispatcher.unregisterInternal(this)
        disposed = true
    }

    class CallbackSubscription<T : Any>
    (dispatcher: Dispatcher, id: Int, priority: Int, tag: Class<T>, cb: (T) -> Unit)
        : DispatcherSubscription<T>(dispatcher, id, priority, tag, cb) {
        override fun dispose() {
            disposeInternal()
        }
    }

    class FlowableSubscription<T : Any>
    (dispatcher: Dispatcher, id: Int, priority: Int, tag: Class<T>, val flowable: PublishProcessor<T>)
        : DispatcherSubscription<T>(dispatcher, id, priority, tag, { a -> flowable.onNext(a) }) {
        override fun dispose() {
            flowable.onComplete()
            disposeInternal()
        }
    }

    class ObservableSubscription<T : Any>
    (dispatcher: Dispatcher, id: Int, priority: Int, tag: Class<T>, val subject: PublishSubject<T>)
        : DispatcherSubscription<T>(dispatcher, id, priority, tag, { a -> subject.onNext(a) }) {
        override fun dispose() {
            subject.onComplete()
            disposeInternal()
        }
    }
}
