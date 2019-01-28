package mini

import java.lang.annotation.Inherited

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
 * Base action that carries [Action] annotation.
 */
@Action
abstract class BaseAction

internal val actionTypesMap: MutableMap<Class<*>, List<Class<*>>> = HashMap()
