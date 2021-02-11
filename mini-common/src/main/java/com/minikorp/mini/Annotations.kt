package com.minikorp.mini

import java.lang.annotation.Inherited

const val DEFAULT_PRIORITY = 100

/**
 * Mark a type as action for code generation. All actions must include this annotation
 * or dispatcher won't work properly.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Action


/**
 * Mark a function declared in a [StateContainer] as a reducer function.
 *
 * Reducers function must have two parameters, the state that must have same time
 * as the [StateContainer] state, and the action being handled.
 *
 * If the reducer function is not pure, only the action parameter is allowed
 * and function should have no return.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reducer(val priority: Int = DEFAULT_PRIORITY)

