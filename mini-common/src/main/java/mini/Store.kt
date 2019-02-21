package mini

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType

/**
 * State holder.
 */
abstract class Store<S : Any> {

    companion object {
        const val INITIALIZE_ORDER_PROP = "store.init.order"
    }

    init {
    }

    val properties: MutableMap<String, Any?> = HashMap()

    private var _state: S? = null

    /** Set new state, equivalent to [asNewState]*/
    protected fun setState(state: S) {
        assertOnUiThread()
        setStateInternal(state)
    }

    /** Hook for write only property */
    protected var newState: S
        get() = throw UnsupportedOperationException("This is a write only property")
        set(value) = setState(value)

    /** Same as property, suffix style */
    protected fun S.asNewState(): S {
        assertOnUiThread()
        setStateInternal(this)
        return this
    }

    val state: S
        get() {
            if (_state == null) {
                synchronized(this) {
                    if (_state == null) {
                        _state = initialState()
                    }
                }
            }
            return _state!!
        }

    private val processor: PublishProcessor<S> = PublishProcessor.create()

    init {
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

    fun flowable(): Flowable<S> {
        return processor.startWith(state)
    }

    private fun setStateInternal(newState: S) {
        //State mutation should to happen on UI thread
        if (newState != _state) {
            _state = newState
            processor.onNext(_state)
        }
    }

    /** Thread safe, since espresso runs in it's own thread */
    @TestOnly
    fun setTestState(other: S) {
        onUiSync {
            setStateInternal(other)
        }
    }
}
