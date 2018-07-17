package mini

import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class DynamicActionReducer : ActionReducer {

    private val subscriptionCounter = AtomicInteger()
    private val subscriptionMap = HashMap<Class<*>, TreeSet<ReducerSubscription<Any>>?>()

    override fun reduce(action: Action) {
        action.tags.forEach { tag ->
            subscriptionMap[tag]?.let { set ->
                set.forEach { it.onAction(action) }
            }
        }
    }

    fun <T : Any, S : Any, ST : Store<S>> subscribe(store: ST,
                                                    klass: KClass<T>,
                                                    priority: Int = DEFAULT_REDUCER_PRIORITY,
                                                    cb: (S, T) -> S): ReducerSubscription<T> {
        val subscription = ReducerSubscription(
            reducer = this@DynamicActionReducer,
            id = subscriptionCounter.getAndIncrement(),
            priority = priority,
            tag = klass.java,
            onAction = { store.setStateInternal(cb(store.state, it)) })
        return registerInternal(subscription)
    }

    private fun <T : Any> registerInternal(dispatcherSubscription: ReducerSubscription<T>): ReducerSubscription<T> {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            subscriptionMap.getOrPut(dispatcherSubscription.tag) {
                TreeSet { a, b ->
                    val p = a.priority.compareTo(b.priority)
                    if (p == 0) a.id.compareTo(b.id)
                    else p
                }
            }!!.add(dispatcherSubscription as ReducerSubscription<Any>)
        }
        return dispatcherSubscription
    }

    internal fun <T : Any> unregisterInternal(dispatcherSubscription: ReducerSubscription<T>) {
        synchronized(this) {
            val set = subscriptionMap[dispatcherSubscription.tag] as? TreeSet<*>
            val removed = set?.remove(dispatcherSubscription) ?: false
            if (!removed) {
                Grove.w { "Failed to remove dispatcherSubscription, multiple dispose calls?" }
            }
        }
    }
}

class ReducerSubscription<T : Any>(private val reducer: DynamicActionReducer,
                                   private val onAction: (T) -> Unit,
                                   internal val tag: Class<T>,
                                   internal val id: Int,
                                   internal val priority: Int) : Disposable {
    private var disposed = false

    internal fun onAction(action: T) {
        if (disposed) {
            Grove.e { "Subscription is disposed but got an action: $action" }
            return
        }
        onAction.invoke(action)
    }

    override fun isDisposed(): Boolean = disposed

    override fun dispose() {
        if (disposed) return
        reducer.unregisterInternal(this)
        disposed = true
    }
}
