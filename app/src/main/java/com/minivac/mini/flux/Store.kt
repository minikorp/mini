package com.minivac.mini.flux

import android.support.annotation.CallSuper
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import java.lang.reflect.ParameterizedType
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class Store<S : Any> : AutoCloseable {

    open val properties: StoreProperties = StoreProperties()

    @Inject protected lateinit var dispatcher: Dispatcher

    private val disposables = CompositeDisposable()
    private var _state: S? = null
    private val processor = PublishProcessor.create<S>()


    var state: S
        get() {
            if (_state == null) _state = initialState()
            return _state!!
        }
        protected set(value) {
            if (value != _state) {
                _state = value
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
    override fun close() {
        disposables.dispose()
    }

    /**
     * Initialize the store. Called after all stores constructors ar
     */
    abstract fun init()
}

/**
 * Type safe store lookup.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Store<*>> StoreMap.find(clazz: KClass<T>): T {
    val java: Class<Store<*>> = (clazz.java) as Class<Store<*>>
    return this[java]!! as T
}

/**
 * Store meta properties.
 *
 * @param initOrder After construction invocation priority. Higher is lower.
 * @param logFn Optional function used to represent state in a human readable form.
 *              If null, current state string function will be used.
 */
data class StoreProperties(
        val initOrder: Int = DEFAULT_INIT_PRIORITY,
        val logFn: (() -> String)? = null) {
    companion object {
        val DEFAULT_INIT_PRIORITY = 100
    }
}