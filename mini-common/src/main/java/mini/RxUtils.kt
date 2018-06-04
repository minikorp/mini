package mini

import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.select(crossinline fn: (T) -> U?): Flowable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Flowable.empty()
        else Flowable.just(mapped)
    }.distinctUntilChanged()
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.select(crossinline fn: (T) -> U?): Observable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Observable.empty()
        else Observable.just(mapped)
    }.distinctUntilChanged()
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