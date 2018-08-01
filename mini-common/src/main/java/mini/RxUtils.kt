package mini

import io.reactivex.Flowable
import io.reactivex.Observable


/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.mapNotNull(crossinline fn: (T) -> U?): Flowable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Flowable.empty()
        else Flowable.just(mapped)
    }
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.mapNotNull(crossinline fn: (T) -> U?): Observable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Observable.empty()
        else Observable.just(mapped)
    }
}

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Flowable<T>.select(crossinline fn: (T) -> U?): Flowable<U> =
        mapNotNull(fn).distinctUntilChanged()

/**
 * Apply the mapping function if object is not null together with a distinctUntilChanged call.
 */
inline fun <T, U> Observable<T>.select(crossinline fn: (T) -> U?): Observable<U> =
        mapNotNull(fn).distinctUntilChanged()

/**
 * Map a [Flowable] emission to take a [Task] in a terminal state. This method should
 * be used to listen [Store] state changes that trigger a screen navigation based on a [Task] request.
 *
 * [onNextTerminalState] just emit once after filter a terminal state, doing this cyclic navigation bugs
 * are avoided listening a [Task] just after dispatch the [Action] that moves it to a [TaskStatus.RUNNING] state.
 */
inline fun <S, D, T : TypedTask<D>> Flowable<S>.onNextTerminalState(crossinline taskMapFn: (S) -> T,
                                                                    crossinline successFn: (S) -> Unit = {},
                                                                    crossinline failureFn: (Throwable) -> Unit = {}) =
        filter { taskMapFn(it).isTerminal() }
                .take(1)
                .subscribe {
                    val task = taskMapFn(it)
                    if (task.isSuccessful()) successFn(it)
                    else failureFn(task.error!!)
                }

/**
 * Transform multiple stores into a single flowable that emits
 * the store that changed instead of the new state.
 */
fun combineStores(vararg stores: Store<*>): Flowable<Store<*>> {
    return stores
            .map { store -> store.flowable().map { store } }
            .reduce { acc, storeFlowable ->
                acc.mergeWith(storeFlowable)
            }
}

/**
 * Builder function for [StateMerger].
 */
inline fun <R> mergeStates(crossinline builder: StateMerger<R>.() -> Unit): Flowable<List<R>> {
    return StateMerger<R>().apply { builder() }.merge()
}

/**
 * Combine multiple store state flowables into a list of type R. If multiple stores
 * change at the same time only one value is emitted.
 */
class StateMerger<R> {
    val storeAndMappers = ArrayList<Pair<Store<*>, () -> R>>()

    inline fun <T : R, S : Store<U>, U : Any> merge(store: S, crossinline mapper: (U.() -> T)) {
        storeAndMappers.add(store to { store.state.mapper() })
    }

    fun merge(): Flowable<List<R>> {
        return storeAndMappers
                .map { (store, fn) -> store.flowable().select { fn() } }
                .reduce { acc, storeFlowable ->
                    acc.mergeWith(storeFlowable)
                }
                .map {
                    storeAndMappers.map { (_, fn) -> fn() }.toList()
                }
    }
}
