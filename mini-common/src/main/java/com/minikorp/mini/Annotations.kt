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


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Reducer(val priority: Int = DEFAULT_PRIORITY)
