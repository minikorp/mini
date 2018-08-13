package mini

const val DEFAULT_REDUCER_PRIORITY = 100

/**
 * Mark a function as a state reducer. This functions are scoped to a Store<T>
 * and have the following restrictions:
 * * Must have at least one parameter for the action.
 * * Optionally a second  parameter for the state.
 * * Must return T (state).
 * * Can't be private or protected .
 *
 * ```
 * class SampleStore : Store<T>() {
 *
 *     @Reducer
 *     fun reduceSample(a: Action): T {
 *         return state
 *     }
 *
 *     @Reducer
 *     fun reduceSampleAlternative(a: Action, state: T): T {
 *         return state
 *     }
 * }
 * ```
 * Reducer functions operate only in the main thread.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reducer(val priority: Int = DEFAULT_REDUCER_PRIORITY)

/**
 * Experimental
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ActionType



