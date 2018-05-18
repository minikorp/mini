package mini

import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType

abstract class Store<S : Any> {

    internal val observers: MutableList<StoreObserver<S>> = ArrayList()
    private var _state: S? = null

    val state: S
        get() {
            if (_state == null) _state = initialState()
            return _state!!
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

    //@Hide
    /**
     * Never call this method.
     */
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

    /**
     * Initialize the store. Called after all stores constructors ar
     */
    abstract fun init()
}

typealias StateCallback<S> = (S) -> Unit

class StoreObserver<S : Any>(private val store: Store<S>,
                             private val cb: StateCallback<S>) {
    internal fun onStateChanged(state: S) {
        cb(state)
    }

    fun dispose() {
        store.observers.remove(this)
    }
}