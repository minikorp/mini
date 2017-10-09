package com.minivac.mini.rx

import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Flowable<T>.view(crossinline fn: (T) -> U?): Flowable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Flowable.empty()
        else Flowable.just(mapped)
    }.distinctUntilChanged()
}

/**
 * Apply the mapping function if object is not null.
 */
inline fun <T, U> Observable<T>.view(crossinline fn: (T) -> U?): Observable<U> {
    return flatMap {
        val mapped = fn(it)
        if (mapped == null) Observable.empty()
        else Observable.just(mapped)
    }.distinctUntilChanged()
}