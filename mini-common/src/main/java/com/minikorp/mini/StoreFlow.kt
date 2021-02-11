package com.minikorp.mini

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*


/**
 * Map multiple objects into a list to run an effect on any change.
 */
fun <T, R> Flow<T>.selectMany(vararg mappers: suspend (T) -> R): Flow<List<R>> {
    return this.map { state ->
        mappers.map { fn -> fn(state) }
    }.distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged].
 */
fun <T, R> Flow<T>.select(mapper: suspend (T) -> R): Flow<R> {
    return this.map { mapper(it) }
            .distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged] ignoring null values.
 */
fun <T, R : Any> Flow<T>.selectNotNull(mapper: suspend (T) -> R?): Flow<R> {
    return this.map { mapper(it) }
            .filterNotNull()
            .distinctUntilChanged()
}

/**
 * Emit a value when the filter passes comparing the last emited value and current value.
 */
fun <T> Flow<T>.onEachChange(filter: (prev: T, next: T) -> Boolean, fn: (T) -> Unit): Flow<T> {
    return distinctUntilChanged().runningReduce { prev, next ->
        if (filter(prev, next)) {
            fn(next)
        }
        next
    }
}

/**
 * Emit a value when the value goes `from` from to `to`.
 */
fun <T> Flow<T>.onEachChange(from: T, to: T, fn: (T) -> Unit): Flow<T> {
    return onEachChange({ prev, next -> prev == from && next == to }, fn)
}

/**
 * Emit when the value goes `true` from to `false` (it disables).
 */
fun Flow<Boolean>.onEachDisable(fn: (Boolean) -> Unit): Flow<Boolean> {
    return onEachChange(from = true, to = false, fn)
}

/**
 * Emit when the value goes `false` from to `true` (it enables).
 */
fun Flow<Boolean>.onEachEnable(fn: (Boolean) -> Unit): Flow<Boolean> {
    return onEachChange(from = false, to = true, fn)
}


/**
 * Return the channel that will emit state changes.
 *
 * @param hotStart emit current state when starting.
 */
fun <S : Any> StateContainer<S>.channel(hotStart: Boolean = true,
                                        capacity: Int = Channel.BUFFERED): Channel<S> {
    val channel = Channel<S>(capacity)
    val subscription = subscribe(hotStart) {
        channel.offer(it)
    }
    @Suppress("EXPERIMENTAL_API_USAGE")
    channel.invokeOnClose {
        subscription.close()
    }
    return channel
}

/**
 * Return the flow that will emit state changes.
 *
 * @param hotStart emit current state when starting.
 */
fun <S : Any> StateContainer<S>.flow(hotStart: Boolean = true, capacity: Int = Channel.BUFFERED): Flow<S> {
    return channel(hotStart = hotStart, capacity = capacity).receiveAsFlow()
}
