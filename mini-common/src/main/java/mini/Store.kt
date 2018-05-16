package mini

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType

abstract class Store<S : Any> {

    open val properties: StoreProperties = StoreProperties()

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
        return processor.startWith { s ->
            s.onNext(state)
            s.onComplete()
        }
    }

    @TestOnly
    fun setTestState(other: S) {
        this.state = other
    }

    @TestOnly
    fun resetState() {
        state = initialState()
    }

    /**
     * Initialize the store. Called after all stores constructors ar
     */
    abstract fun init()
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