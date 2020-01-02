package mini

import org.jetbrains.annotations.TestOnly
import java.io.Closeable
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * State holder.
 */
abstract class Store<S> : Closeable, StateContainer<S> {

    companion object {
        val NO_STATE = Any()
    }

    class StoreSubscription internal constructor(private val store: Store<*>,
                                                 private val fn: Any) : Closeable {
        override fun close() {
            store.listeners.remove(fn)
        }
    }

    private var _state: Any? = NO_STATE
    private val listeners = Vector<(S) -> Unit>()

    /** Set new state, equivalent to [asNewState]*/
    protected fun setState(state: S) {
        assertOnUiThread()
        performStateChange(state)
    }

    /** Hook for write only property */
    protected var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) = setState(value)

    /** Same as property, suffix style */
    protected fun S.asNewState(): S {
        assertOnUiThread()
        performStateChange(this)
        return this
    }

    fun subscribe(hotStart: Boolean = true, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return StoreSubscription(this, fn)
    }

    override val state: S
        get() {
            if (_state === NO_STATE) {
                synchronized(this) {
                    if (_state === NO_STATE) {
                        _state = initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }

    /**
     * Initialize the store after dependency injection is complete.
     */
    open fun initialize() {
        //No-op
    }

    @Suppress("UNCHECKED_CAST")
    open fun initialState(): S {
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

    private fun performStateChange(newState: S) {
        //State mutation should to happen on UI thread
        if (newState != _state) {
            _state = newState
            listeners.forEach {
                it(newState)
            }
        }
    }

    /** Test only method, don't use in app code */
    @TestOnly
    fun setTestState(s: S) {
        if (isAndroid) {
            onUiSync {
                performStateChange(s)
            }
        } else {
            performStateChange(s)
        }
    }

    /** Set state back to initial default */
    @TestOnly
    fun resetState() {
        setTestState(initialState())
    }

    final override fun close() {
        listeners.clear() //Remove all listeners
        onClose()
    }

    open fun onClose() = Unit
}
