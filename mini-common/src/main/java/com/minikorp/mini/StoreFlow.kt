package com.minikorp.mini

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged].
 */
fun <T, R> Flow<T>.select(mapper: suspend (T) -> R): Flow<R> {
    return this
            .map { mapper(it) }
            .distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged] ignoring null values.
 */
fun <T, R : Any> Flow<T>.selectNotNull(mapper: suspend (T) -> R?): Flow<R> {
    return this
            .map { mapper(it) }
            .filterNotNull()
            .distinctUntilChanged()
}

/**
 * Return the channel that will emit state changes.
 *
 * @param hotStart emit current state when starting.
 */
fun <S : Any> Store<S>.channel(hotStart: Boolean = true,
                               capacity: Int = Channel.BUFFERED): Channel<S> {
    val channel = Channel<S>(capacity)
    val subscription = subscribe(hotStart) {
        channel.offer(it)
    }
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
fun <S : Any> Store<S>.flow(hotStart: Boolean = true, capacity: Int = Channel.BUFFERED): Flow<S> {
    return channel(hotStart = hotStart, capacity = capacity).consumeAsFlow()
}
