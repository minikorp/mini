package mini

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Container of a dispatcher that has a lifecycle attached to it (Android Fragment / Activity).
 */
interface DispatcherContainer {

    val dispatcher: Dispatcher

    /**
     * Scope where [defaultDispatchScope] actions will be called.
     * Cancelling this scope will also cancel the any ongoing dispatch.
     */
    val defaultDispatchScope: CoroutineScope

    /**
     * Dispatch an action in the provided [defaultDispatchScope] and call [onComplete] when
     * dispatching is complete.
     *
     * Use [kotlinx.coroutines.GlobalScope] if you want dispatch to continue after view is gone.
     *
     * @return A cancellable job.
     */
    fun dispatch(action: Any, context: CoroutineContext = EmptyCoroutineContext,
                 scope: CoroutineScope = defaultDispatchScope,
                 start: CoroutineStart = CoroutineStart.UNDISPATCHED,
                 onComplete: suspend () -> Unit = {}): Job {
        return scope.launch(start = start, context = context) {
            dispatcher.dispatch(action)
            onComplete()
        }
    }
}