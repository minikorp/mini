package mini

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType

/**
 * State holder.
 */
abstract class Store<S : Any> {

    private val channel = BroadcastChannel<S>(Channel.UNLIMITED)
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

    private fun setStateInternal(newState: S) {
        //State mutation should to happen on UI thread
        if (newState != _state) {
            _state = newState
            channel.offer(newState)
        }
    }

    fun flow(): Flow<S> {
        return channel.openSubscription().consumeAsFlow()
    }

    fun channel(): ReceiveChannel<S> {
        return channel.openSubscription()
    }

    /** Test only method, don't use in app code */
    @TestOnly
    fun setTestState(s: S) {
        if (isAndroid) {
            onUiSync {
                setStateInternal(s)
            }
        } else {
            setStateInternal(s)
        }
    }

    /** Set state back to initial default */
    @TestOnly
    fun resetState() {
        setTestState(initialState())
    }
}
