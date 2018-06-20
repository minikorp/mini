package mini

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

typealias StateCallback<S> = (S) -> Unit

abstract class Store<S : Any> {

    companion object {
        const val INITIALIZE_ORDER_PROP = "store.init.order"
    }

    val properties: MutableMap<String, Any?> = HashMap()

    private var _state: S? = null
    val state: S
        get() {
            if (_state == null) _state = initialState()
            return _state!!
        }

    private val observers: MutableList<StoreObserver<S>> = ArrayList()
    private val processor: PublishProcessor<S> = PublishProcessor.create()
    private val processorObserver: StateCallback<S> = object : StateCallback<S> {
        override fun invoke(state: S) {
            processor.onNext(state)
        }
    }

    init {
        observe(processorObserver)
        properties[INITIALIZE_ORDER_PROP] = 100
    }

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun initialState(): S {
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

    fun observe(cb: StateCallback<S>): StoreObserver<S> {
        val observer = StoreObserver(this, cb)
        observers.add(observer)
        return observer
    }

    fun flowable(): Flowable<S> {
        return processor
    }

    /**
     * This a private api that needs to be public for code-gen purposes.
     * Never call this method.
     * */
    fun setStateInternal(newState: S) {
        if (newState != _state) {
            _state = newState
            observers.forEach {
                it.onStateChanged(state)
            }
        }
    }

    @TestOnly
    fun setTestState(other: S) {
        setStateInternal(other)
    }

    @TestOnly
    fun resetState() {
        setStateInternal(initialState())
    }

    /** Utility alias for no code-gen subscription */
    fun <T : Any> DynamicActionReducer.subscribe(klass: KClass<T>,
                                                 priority: Int = DEFAULT_REDUCER_PRIORITY,
                                                 cb: (S, T) -> S): ReducerSubscription<T> {
        return this.subscribe(this@Store, klass, priority, cb)
    }

    class StoreObserver<S : Any>(private val store: Store<S>,
                                 private val cb: StateCallback<S>) {
        internal fun onStateChanged(state: S) {
            cb(state)
        }

        fun dispose() {
            store.observers.remove(this)
        }
    }
}
