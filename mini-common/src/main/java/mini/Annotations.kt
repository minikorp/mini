package mini

import java.lang.annotation.Inherited

const val DEFAULT_PRIORITY = 100

/**
 * Mark a type as action for code generation. All actions must include this annotation
 * or dispatcher won't work properly.
 *
 * This action is inherited by [BaseAction] or just added directly.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Action

/**
 * Actions implementing this interface are not supposed to change state, just wrap
 * other dispatch calls, state diff won't be logged
 */
@Action
interface SagaAction


/**
 * Mark function as reducer function for codegen.
 * One argument for action is expected, and any return value will be ignored.
 *
 * This function is not required to be pure but you should aim for it to be so, however,
 * for compatibility with multiple state management options state can be obtained
 * from any source (LiveData, Store, ViewModel).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reducer(val priority: Int = DEFAULT_PRIORITY)

/**
 * Alias for [Reducer] for better readability.
 */
typealias Saga = Reducer
