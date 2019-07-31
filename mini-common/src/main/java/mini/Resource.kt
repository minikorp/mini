@file:Suppress("UNCHECKED_CAST")

package mini

/**
 * Simple wrapper to map ongoing tasks (network / database) for view implementation.
 *
 * Similar to kotlin [Result] but with loading and empty state.
 */
open class Resource<out T> @PublishedApi internal constructor(val value: Any?) {

    val isSuccess: Boolean get() = !isLoading && !isFailure && !isEmpty
    val isEmpty: Boolean get() = value is Empty
    val isFailure: Boolean get() = value is Failure
    val isLoading: Boolean get() = value is Loading<*>

    internal class Empty
    internal class Failure(val exception: Throwable?)
    internal class Loading<U>(val value: U? = null)

    /**
     * Get the current value if successful, or null for other cases.
     */
    fun getOrNull(): T? =
        when {
            isSuccess -> value as T?
            else -> null
        }

    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    override fun toString(): String =
        when (value) {
            is Failure -> value.toString() // "Failure($exception)"
            else -> "Success($value)"
        }

    companion object {
        fun <T> success(value: T): Resource<T> = Resource(value)
        fun <T> failure(exception: Throwable? = null): Resource<T> = Resource(Failure(exception))
        fun <T> loading(value: T? = null): Resource<T> = Resource(Loading(value))
        fun <T> empty(): Resource<T> = Resource(Empty())
    }
}

/**
 * An empty resource that just abstracts asynchronous operation but with idle
 * state instead of empty.
 */
class Task(value: Any?) : Resource<Unit>(value) {
    val isIdle: Boolean get() = isEmpty

    companion object {
        fun success(): Task = Task(Unit)
        fun idle(): Task = Task(Empty())
        fun loading(): Task = Task(Loading<Unit>())
        fun failure(exception: Throwable? = null): Task = Task(Failure(exception))
    }
}

inline fun <T> Resource<T>.onSuccess(crossinline action: (data: T) -> Unit): Resource<T> {
    if (isSuccess) action(value as T)
    return this
}

inline fun <T> Resource<T>.onFailure(crossinline action: (data: T) -> Unit): Resource<T> {
    if (isFailure) action(value as T)
    return this
}

inline fun <T> Resource<T>.onLoading(crossinline action: (data: T) -> Unit): Resource<T> {
    if (isLoading) action(value as T)
    return this
}

inline fun Task.onIdle(crossinline action: () -> Unit): Task {
    onEmpty { action() }
    return this
}

inline fun <T> Resource<T>.onEmpty(crossinline action: (data: T) -> Unit): Resource<T> {
    if (isEmpty) action(value as T)
    return this
}

inline fun <T, R> Resource<T>.map(crossinline transform: (data: T) -> R): Resource<R> {
    if (isSuccess) return Resource.success(transform(value as T))
    return Resource(value)
}

/** All tasks succeeded. */
fun <T> Iterable<Resource<T>>.allSuccesful(): Boolean {
    return this.all { it.isSuccess }
}

/** Any tasks failed. */
fun <T> Iterable<Resource<T>>.anyFailure(): Boolean {
    return this.any { it.isFailure }
}

/** Any task is running. */
fun <T> Iterable<Resource<T>>.anyLoading(): Boolean {
    return this.any { it.isLoading }
}