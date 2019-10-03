package mini.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import mini.Store

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged].
 */
@FlowPreview
fun <T, R> Flow<T>.select(mapper: suspend (T) -> R): Flow<R> {
    return this
        .map { mapper(it) }
        .distinctUntilChanged()
}

/**
 * Combination of [Flow.map] and [Flow.distinctUntilChanged] ignoring null values.
 */
@FlowPreview
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
@FlowPreview
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
@FlowPreview
fun <S : Any> Store<S>.flow(hotStart: Boolean = true, capacity: Int = Channel.BUFFERED): Flow<S> {
    return channel(hotStart = hotStart, capacity = capacity).consumeAsFlow()
}
