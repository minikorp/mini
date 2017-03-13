package com.minivac.mini.flux

import android.support.annotation.CallSuper
import com.minivac.mini.log.Grove
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class Store<S : Any> : Disposable {

    open val properties: StoreProperties = StoreProperties()

    private val disposables = CompositeDisposable()
    private var internalState: S? = null
    private val processor = PublishProcessor.create<S>()

    var state: S
        get() {
            if (internalState == null) internalState = initialState()
            return internalState!!
        }
        protected set(value) {
            if (value != internalState) {
                internalState = value
                processor.onNext(value)
            }
        }

    @Suppress("UNCHECKED_CAST")
    protected open fun initialState(): S {
        try {
            val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                    as Class<S>
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state", e)
        }
    }

    /**
     * Initialize the store. Called after all stores constructors ar
     */
    abstract fun init()

    fun flowable(): Flowable<S> {
        return processor.startWith { s ->
            s.onNext(state)
            s.onComplete()
        }
    }

    fun Disposable.track() {
        disposables.add(this)
    }

    @CallSuper
    override fun dispose() {
        disposables.clear()
    }

    override fun isDisposed() = disposables.isDisposed
}

data class StoreProperties(
        val initOrder: Int = DEFAULT_INIT_PRIORITY
) {
    companion object {
        val DEFAULT_INIT_PRIORITY = 100
    }
}

fun initStores(stores: List<Store<*>>) {
    val now = System.currentTimeMillis()

    Collections.sort(stores) { o1, o2 ->
        Integer.compare(
                o1.properties.initOrder,
                o2.properties.initOrder)
    }

    val initTimes = LongArray(stores.size)

    for (i in 0..stores.size - 1) {
        val start = System.currentTimeMillis()
        stores[i].init()
        stores[i].state //Create initial state
        initTimes[i] += System.currentTimeMillis() - start
    }

    val elapsed = System.currentTimeMillis() - now

    Grove.d { "┌ Application with ${stores.size} stores loaded in $elapsed ms" }
    Grove.d { "├────────────────────────────────────────────" }
    for (i in 0..stores.size - 1) {
        val store = stores[i]
        var boxChar = "├"
        if (store === stores[stores.size - 1]) {
            boxChar = "└"
        }
        Grove.d { "$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms" }
    }
}