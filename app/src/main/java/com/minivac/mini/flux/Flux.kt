package com.minivac.mini.flux

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

interface Action {
    val tags: Array<Class<*>>
        get() = arrayOf(Any::class.java, this.javaClass)
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

    private val subscriptionMap = HashMap<Class<*>, TreeSet<Subscription<Any>>?>()
    private var subscriptionCounter = AtomicInteger()
    val subscriptionCount: Int get() = subscriptionCounter.get()

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
            chain.proceed(action)
        }
    }

    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    fun dispatchOnUiSync(action: Action) {
        if (verifyThreads) assertNotOnUiThread()
        onUiSync { dispatch(action) }
    }

    fun <T : Any> subscribeObservable(tag: KClass<T>, fn: (Observable<T>) -> Unit)
            = subscribeObservable(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> subscribeObservable(priority: Int = DEFAULT_PRIORITY,
                                      tag: KClass<T>,
                                      fn: (Observable<T>) -> Unit): Subscription<T> {
        val subject = PublishSubject.create<T>()
        val subscription = Subscription.ObservableSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                subject)
        fn.invoke(subject)
        return registerInternal(subscription)
    }

    fun <T : Any> subscribeFlowable(tag: KClass<T>, fn: (Flowable<T>) -> Unit)
            = subscribeFlowable(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> subscribeFlowable(priority: Int,
                                    tag: KClass<T>,
                                    fn: (Flowable<T>) -> Unit): Subscription<T> {
        val processor = PublishProcessor.create<T>()
        val subscription = Subscription.FlowableSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                processor)
        fn.invoke(processor)
        return registerInternal(subscription)
    }

    fun <T : Any> subscribe(tag: KClass<T>, fn: (T) -> Unit)
            = subscribe(DEFAULT_PRIORITY, tag, fn)

    fun <T : Any> subscribe(priority: Int,
                            tag: KClass<T>,
                            fn: (T) -> Unit): Subscription<T> {
        val subscription = Subscription.CallbackSubscription(
                this,
                subscriptionCounter.getAndIncrement(),
                priority,
                tag.java,
                fn)
        return registerInternal(subscription)
    }

    internal fun <T : Any> registerInternal(subscription: Subscription<T>): Subscription<T> {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            subscriptionMap.getOrPut(subscription.tag, {
                TreeSet({ a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                })
            })!!.add(subscription as Subscription<Any>)
        }
        return subscription
    }

    internal fun <T : Any> unregisterInternal(subscription: Subscription<T>) {
        synchronized(this) {
            val set = subscriptionMap[subscription.tag] as? TreeSet<*>
            set?.remove(subscription)
        }
    }
}

sealed class Subscription<T : Any>(internal val dispatcher: Dispatcher,
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
        : Subscription<T>(dispatcher, id, priority, tag, cb) {
        override fun dispose() {
            disposeInternal()
        }
    }

    class FlowableSubscription<T : Any>
    (dispatcher: Dispatcher, id: Int, priority: Int, tag: Class<T>, val flowable: PublishProcessor<T>)
        : Subscription<T>(dispatcher, id, priority, tag, { a -> flowable.onNext(a) }) {
        override fun dispose() {
            flowable.onComplete()
            disposeInternal()
        }
    }

    class ObservableSubscription<T : Any>
    (dispatcher: Dispatcher, id: Int, priority: Int, tag: Class<T>, val subject: PublishSubject<T>)
        : Subscription<T>(dispatcher, id, priority, tag, { a -> subject.onNext(a) }) {
        override fun dispose() {
            subject.onComplete()
            disposeInternal()
        }
    }
}
