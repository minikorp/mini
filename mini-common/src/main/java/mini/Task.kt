package mini

/** State of the task. Idle is not a terminal state*/
enum class TaskStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    ERROR
}

/**
 * Basic object to represent an ongoing process.
 */
data class TypedTask<out T>(val status: TaskStatus = TaskStatus.IDLE,
                            val metadata: T,
                            val error: Throwable? = null) {

    @Suppress("UndocumentedPublicFunction")
    fun isRunning() = status == TaskStatus.RUNNING

    @Suppress("UndocumentedPublicFunction")
    fun isFailure() = status == TaskStatus.ERROR

    @Suppress("UndocumentedPublicFunction")
    fun isTerminal(): Boolean = status == TaskStatus.SUCCESS || status == TaskStatus.ERROR

    @Suppress("UndocumentedPublicFunction")
    fun isSuccessful() = status == TaskStatus.SUCCESS
}

typealias Task = TypedTask<Nothing?>

//Factory functions

/** Idle task **/
fun <T> taskIdle(data: T): TypedTask<T> = TypedTask(TaskStatus.IDLE, data, null)

/** Sets the task as succeeded with data. */
fun <T> taskSuccess(data: T): TypedTask<T> = TypedTask(TaskStatus.SUCCESS, data, null)

/** Sets the task as running. */
fun <T> taskRunning(data: T): TypedTask<T> = TypedTask(TaskStatus.RUNNING, data, null)

/** Sets the task as error, with its cause. */
fun <T> taskFailure(data: T, error: Throwable? = null): TypedTask<T> = TypedTask(TaskStatus.ERROR, data, error)

//Factory functions for nullable types

/** Idle task **/
fun <T> taskIdle(): TypedTask<T?> = TypedTask(TaskStatus.IDLE, null, null)

/** Tasks success for nullable types */
fun <T> taskSuccess(): TypedTask<T?> = TypedTask(TaskStatus.SUCCESS, null, null)

/** Tasks running for nullable types */
fun <T> taskRunning(): TypedTask<T?> = TypedTask(TaskStatus.RUNNING, null, null)

/** Tasks error for nullable types */
fun <T> taskFailure(error: Throwable? = null): TypedTask<T?> = TypedTask(TaskStatus.ERROR, null, error)

//Utilities for task collections

/** Find the first failed task or throw an exception. */
fun <T> Iterable<TypedTask<T>>.firstFailure(): TypedTask<T> {
    return this.first { it.isFailure() && it.error != null }
}

/** Find the first failed task or null. */
fun <T> Iterable<TypedTask<T>>.firstFailureOrNull(): TypedTask<T>? {
    return this.firstOrNull { it.isFailure() && it.error != null }
}

/** All task are in terminal state. */
fun <T> Iterable<TypedTask<T>>.allCompleted(): Boolean {
    return this.all { it.isTerminal() }
}

/** All tasks succeeded. */
fun <T> Iterable<TypedTask<T>>.allSuccesful(): Boolean {
    return this.all { it.isSuccessful() }
}

/** Any tasks failed. */
fun <T> Iterable<TypedTask<T>>.anyFailure(): Boolean {
    return this.any { it.isFailure() }
}

/** Any task is running. */
fun <T> Iterable<TypedTask<T>>.anyRunning(): Boolean {
    return this.any { it.isRunning() }
}
